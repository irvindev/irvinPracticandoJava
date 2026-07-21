package com.pe.allpafood.api.gateway.admin.menu;


import com.pe.allpafood.api.core.utils.dto.GenericMessage;
import com.pe.allpafood.api.transaction.catalog.bussiness.IComplementMenus;
import com.pe.allpafood.api.transaction.catalog.bussiness.IMenusService;
import com.pe.allpafood.api.transaction.catalog.bussiness.impl.ScheduleMenu;
import com.pe.allpafood.api.transaction.catalog.dto.MenuFormDTO;
import com.pe.allpafood.api.transaction.catalog.dto.ScheduleMenuDTO;
import com.pe.allpafood.api.transaction.catalog.entity.MenuCalendar;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final IMenusService menuService;
    private final IComplementMenus complementMenus;
    private final ScheduleMenu scheduleMenu;

    @GetMapping
    public ResponseEntity<List<MenuEntity>> getAllMenu() {
        return ResponseEntity.ok(menuService.getAllDishes());
    }

    @PostMapping
    public ResponseEntity<MenuEntity> postMenu(@ModelAttribute MenuFormDTO dish) throws IOException {
        return ResponseEntity.ok(menuService.save(dish));
    }

    @PutMapping
    public ResponseEntity<MenuEntity> putMenu(@ModelAttribute MenuFormDTO dish) throws IOException {
        return ResponseEntity.ok(menuService.save(dish));
    }

    @GetMapping("/complements")
    public ResponseEntity<List<MenuEntity>> getExtras(){
        return ResponseEntity.ok(complementMenus.getAll());
    }

    @PostMapping("/schedule")
    public ResponseEntity<GenericMessage> scheduleMenu(@RequestBody ScheduleMenuDTO dto) {
        scheduleMenu.schedule(dto);
        return ResponseEntity.ok(new GenericMessage("ok"));
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<MenuCalendar>> getScheduledMenu(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
            ) {

        return ResponseEntity.ok(scheduleMenu.getScheduledMenu(startDate,endDate));
    }
}
