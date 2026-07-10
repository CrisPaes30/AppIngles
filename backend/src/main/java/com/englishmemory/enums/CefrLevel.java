package com.englishmemory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CefrLevel {
    A1("Beginner"),
    A2("Elementary"),
    B1("Intermediate"),
    B2("Upper-Intermediate"),
    C1("Advanced"),
    C2("Proficiency");

    private final String description;
}
