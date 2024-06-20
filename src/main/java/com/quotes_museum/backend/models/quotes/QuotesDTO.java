package com.quotes_museum.backend.models.quotes;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class QuotesDTO{

    private final int id;
    private final String quote;
    private final boolean obscene;
    private final int cites;
    private final String[] features;
    private final Map<String, String> attrs;
    private final String owner;

}
