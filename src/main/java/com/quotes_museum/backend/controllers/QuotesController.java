package com.quotes_museum.backend.controllers;

import com.quotes_museum.backend.models.quotes.QuotesDTO;
import com.quotes_museum.backend.services.QuotesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/quotes")
@PreAuthorize("hasAnyRole('FARDA', 'GUEST', 'USER')")
@RequiredArgsConstructor
public class QuotesController {

    @Autowired
    private final QuotesService quotesService;

    @GetMapping("/core-table")
    public @ResponseBody List<String> getCoreTable(String table) throws SQLException {
        return quotesService.getCoreTable(table);
    }

    @GetMapping("/search")
    public @ResponseBody List<QuotesDTO> searchQuote(String quote) throws SQLException {
        return quotesService.searchQuote(quote);
    }

    @GetMapping("/owners")
    public @ResponseBody List<QuotesDTO> getOwnerQuotes(String owner) throws SQLException {
        return quotesService.getOwnerQuotes(owner);
    }

    @GetMapping("/random")
    public @ResponseBody QuotesDTO getRandomQuote() throws SQLException {
        return quotesService.getRandomQuote();
    }
}
