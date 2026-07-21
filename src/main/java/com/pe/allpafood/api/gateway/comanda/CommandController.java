package com.pe.allpafood.api.gateway.comanda;

import com.pe.allpafood.api.transaction.catalog.dto.comanda.ComandaDTO;
import com.pe.allpafood.api.transaction.order.bussiness.command.CommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("admin")
@RequiredArgsConstructor
public class CommandController {

    private final CommandService commandService;

    @GetMapping("/command/dashboard")
    public ResponseEntity<ComandaDTO> getCommandDashboard(@RequestParam LocalDate date){
        ComandaDTO response = commandService.getCommandByDate(date);
        return ResponseEntity.ok(response);
    }
}
