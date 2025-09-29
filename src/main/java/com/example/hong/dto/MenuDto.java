package com.example.hong.dto;

import com.example.hong.entity.CafeMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuDto {
    private String name;
    private Integer price;

    public static MenuDto fromEntity(CafeMenu menu) {
        return new MenuDto(menu.getName(), menu.getPrice());
    }
}
