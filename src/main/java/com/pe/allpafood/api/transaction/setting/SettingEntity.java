package com.pe.allpafood.api.transaction.setting;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SettingEntity {
    private Long id;
    private String name;
    private List<String> value;
}
