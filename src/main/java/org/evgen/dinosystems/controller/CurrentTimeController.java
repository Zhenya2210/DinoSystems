package org.evgen.dinosystems.controller;

import org.evgen.dinosystems.controller.dto.TimeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class CurrentTimeController {


    @GetMapping(path = "/time/current")
    public TimeResponse getCurrentTime(@RequestParam("time_offset") String timeOffset) {

        return new TimeResponse(new Date(), timeOffset);
    }

}
