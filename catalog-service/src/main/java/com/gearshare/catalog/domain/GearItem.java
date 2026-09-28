package com.gearshare.catalog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "gear_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GearItem {

    @Id
    private String id;

    private String ownerId;

    private String title;

    private BigDecimal dailyPrice;

    /** Whether the item is currently free to be held. Flipped to false while a hold is active. */
    private boolean available;
}
