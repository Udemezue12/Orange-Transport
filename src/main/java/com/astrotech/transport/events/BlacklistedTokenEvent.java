package com.astrotech.transport.events;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlacklistedTokenEvent {

    private String jti;
    private Date expiration;
}
