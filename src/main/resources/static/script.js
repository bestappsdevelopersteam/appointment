


document.addEventListener('DOMContentLoaded', function () {
    var searchNameInput = document.getElementById('searchName');
    var searchPhoneInput = document.getElementById('searchPhone');

    searchNameInput.addEventListener('keyup', function (event) {
        if (event.key === 'Enter') {
            searchAppointments('name', searchNameInput.value);
        }
    });

    searchPhoneInput.addEventListener('keyup', function (event) {
        if (event.key === 'Enter') {
            searchAppointments('phone', searchPhoneInput.value);
        }
    });

    function searchAppointments(criteria, value) {
        if (!value.trim()) return;

        fetch(`/admin/appointments/search?${criteria}=${encodeURIComponent(value)}`)
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error('Грешка при търсене: ' + response.statusText);
                }
            })
            .then(data => {
                if (data.length > 0) {
                    var lastAppointment = data[data.length - 1];

                    // Попълваме формата с намерения запис
                    document.getElementById('name').value = lastAppointment.name;
                    document.getElementById('phone').value = lastAppointment.phone;
                    document.getElementById('email').value = lastAppointment.email;
                    document.getElementById('appointmentDate').value = lastAppointment.appointmentDate;
                    document.getElementById('appointmentTime').value = lastAppointment.appointmentTime;
                    document.getElementById('additionalInfo').value = lastAppointment.additionalInfo;

                    // Запазваме ID на намерения запис във формата
                    document.getElementById('appointment-form').dataset.appointmentId = lastAppointment.id;

                    // Активиране на бутона за изтриване
                    document.getElementById('delete-appointment-button').disabled = false;

                    // Автоматично селектиране на събитието в календара
                    highlightAndSelectEvent(lastAppointment.id, lastAppointment.name, lastAppointment.phone);

                    alert(`Намерени са ${data.length} записа. Зареждане на последния преглед.`);
                } else {
                    alert('Не са намерени прегледи по зададените критерии.');
                }
            })
            .catch(error => console.error('Грешка при търсене на преглед:', error));
    }

    function highlightAndSelectEvent(eventId, name, phone) {
        var calendarEvents = calendar.getEvents();

        // Нулираме всички събития преди новото маркиране
        calendarEvents.forEach(event => {
            event.setProp('backgroundColor', '');
            event.setProp('borderColor', '');
        });

        // Маркираме и селектираме намереното събитие
        calendarEvents.forEach(event => {
            if (event.id === String(eventId) || (event.title === name && event.extendedProps.phone === phone)) {
                event.setProp('backgroundColor', '#FFD700'); // Жълт цвят за маркиране
                event.setProp('borderColor', '#FFA500'); // Оранжев цвят за рамка

                // Селектиране на събитието и показване на детайлите
                var info = { event: event };
                calendar.trigger('eventClick', info);
                console.log(`Автоматично селектирано събитие с ID: ${eventId}`);
            }
        });
    }

    var appointmentDateInput = document.getElementById('appointmentDate');
    appointmentDateInput.min = new Date().toISOString().split("T")[0];

    // Добавяме стъпка от 5 минути за полето за време
    var appointmentTimeInput = document.getElementById('appointmentTime');
    appointmentTimeInput.step = 300; // 300 секунди = 5 минути

    var calendarEl = document.getElementById('calendar');
    var calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'bg',
        slotLabelInterval: '00:05',  // Интервал на времевите слотове през 5 минути
        slotDuration: '00:05:00',    // Продължителност на времевите слотове е 5 минути
        events: fetchAppointments,
        editable: true,
        eventClick: function (info) {
            var appointment = info.event.extendedProps;
            document.getElementById('name').value = info.event.title;
            document.getElementById('phone').value = appointment.phone || '';
            document.getElementById('email').value = appointment.email || '';
            document.getElementById('appointmentDate').value = info.event.startStr.split("T")[0];
            var time = info.event.startStr.split("T")[1];
            document.getElementById('appointmentTime').value = time.slice(0, 5);  // Показваме времето като HH:MM
            document.getElementById('additionalInfo').value = appointment.description || '';

            document.getElementById('appointment-form').style.display = 'block';
            document.getElementById('delete-appointment-button').disabled = false;
            document.getElementById('appointment-form').dataset.appointmentId = info.event.id;
            console.log("Избран е съществуващ преглед с ID: " + info.event.id);
        }
    });
    calendar.render();

    function fetchAppointments(fetchInfo, successCallback, failureCallback) {
        fetch('/admin/appointments')
            .then(response => response.json())
            .then(data => {
                var events = data.map(appointment => ({
                    id: appointment.id,
                    title: appointment.name,
                    start: appointment.appointmentDate + 'T' + appointment.appointmentTime,
                    description: appointment.additionalInfo,
                    email: appointment.email,
                    phone: appointment.phone
                }));
                successCallback(events);
            })
            .catch(error => failureCallback(error));
    }

    document.getElementById('add-new-appointment-link').addEventListener('click', function (e) {
        e.preventDefault();

        // Изчистване на полетата за търсене
        document.getElementById('searchName').value = '';
        document.getElementById('searchPhone').value = '';

        // Нулиране на формата за добавяне на нов преглед
        resetForm();
        console.log("Добавяне на нов преглед. Формата е в ново състояние, а полетата за търсене са изчистени.");
    });

document.getElementById('appointment-form').addEventListener('submit', function (e) {
    e.preventDefault();

    var appointmentId = this.dataset.appointmentId;
    var method = appointmentId ? 'PUT' : 'POST';
    var url = appointmentId ? '/admin/appointments/' + appointmentId : '/admin/appointments';

    var appointmentDate = document.getElementById('appointmentDate').value;

    // Проверка за стара дата САМО ако създаваме нов преглед
    var today = new Date().toISOString().split("T")[0];
    if (!appointmentId && appointmentDate < today) {
        alert('Не може да добавите нов преглед със стара дата.');
        return;
    }

    // Събиране на информацията за прегледа
    var appointment = {
        name: document.getElementById('name').value,
        phone: document.getElementById('phone').value,
        email: document.getElementById('email').value,
        appointmentDate: appointmentDate,
        appointmentTime: document.getElementById('appointmentTime').value,
        additionalInfo: document.getElementById('additionalInfo').value
    };

    // Изпращане на заявката за нов или съществуващ преглед
    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(appointment)
    })
    .then(response => response.json())
    .then(data => {
        alert(data.message);
        calendar.refetchEvents();
        resetForm();
    })
    .catch(error => console.error('Грешка при запис на преглед:', error));
});



    function resetForm() {
        document.getElementById('appointment-form').reset();
        document.getElementById('appointment-form').style.display = 'inline-block';
        document.getElementById('appointment-form').dataset.appointmentId = '';
        document.getElementById('delete-appointment-button').disabled = true;
    }

    document.getElementById('delete-appointment-button').addEventListener('click', function () {
        var appointmentId = document.getElementById('appointment-form').dataset.appointmentId;
        if (appointmentId && confirm('Сигурни ли сте, че искате да изтриете този преглед?')) {
            fetch('/admin/appointments/' + appointmentId, { method: 'DELETE' })
                .then(() => {
                    alert('Прегледът беше успешно изтрит.');
                    calendar.refetchEvents();
                    resetForm();
                })
                .catch(error => console.error('Грешка при изтриване на преглед:', error));
        }
    });
});
