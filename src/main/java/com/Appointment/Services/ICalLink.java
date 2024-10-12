package com.Appointment.Services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.Appointment.Entities.Appointment;
import org.springframework.stereotype.Service;

@Service
public class ICalLink {

    public String generateICalLink(Appointment appointment) {
        String baseUrl = "https://www.google.com/calendar/render?action=TEMPLATE";
        String title = URLEncoder.encode("Преглед при д-р Атанасова", StandardCharsets.UTF_8);
        String date = appointment.getAppointmentDate() + "T" + appointment.getAppointmentTime() + ":00";
        String url = baseUrl +
                "&text=" + title +
                "&dates=" + date + "/" + date +
                "&details=" + URLEncoder.encode(appointment.getAdditionalInfo(), StandardCharsets.UTF_8);
        return url;
    }
}
