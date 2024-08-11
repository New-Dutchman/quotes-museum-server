package com.quotes_museum.backend.controllers;

import com.quotes_museum.backend.models.quotes.QuotesDTO;
import com.quotes_museum.backend.services.QuotesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/inside")
@PreAuthorize("hasAnyRole('FARDA', 'USER')")
@RequiredArgsConstructor
public class AccountController {

    @Autowired
    private final QuotesService quotesService;

    @GetMapping("/fav-quotes")
    public @ResponseBody List<QuotesDTO> getFavQuotes(Principal principal) throws SQLException {
        return quotesService.getFavQuotes(principal);
    }

    @GetMapping("/owning")
    public @ResponseBody List<QuotesDTO> getAddedCards(Principal principal) throws SQLException {
        return quotesService.getAddedQuotes(principal);
    }

    @PutMapping("/add-fav")
    public @ResponseBody boolean addFavouriteQuote(@RequestBody @Valid QuotesDTO quote, Principal principal){
        return quotesService.addFavouriteQuote(principal, quote);
    }

    @DeleteMapping("/remove-fav")
    public @ResponseBody boolean removeFavouriteQuote(@Valid int quoteId, Principal principal) {
        return quotesService.removeFavouriteQuote(principal, quoteId);
    }

    @PostMapping("/add-quote")
    public @ResponseBody boolean addQuote(@RequestBody @Valid QuotesDTO quote, Principal principal) {
        return quotesService.insertQuote(quote, principal);
    }

    @PutMapping("/update-quote")
    public @ResponseBody String updateQuote(@RequestBody @Valid UpdateQuote updateQuote, Principal principal) {
        return quotesService.updateQuote(updateQuote.quote,
                updateQuote.quoteChanged,
                updateQuote.attrsChanged,
                updateQuote.featuresChanged,
                principal);
    }

    @PostMapping("/add-owner")
    public @ResponseBody boolean addOwner(@RequestBody @Valid Owner owner, Principal principal) {
        return quotesService.addOwner(owner.owner, owner.description, principal);
    }

    @DeleteMapping("/delete-quote")
    public @ResponseBody boolean deleteQuote(int id, Principal principal) {
        return quotesService.deleteQuote(id, principal);
    }

    private record UpdateQuote(QuotesDTO quote, boolean attrsChanged, boolean featuresChanged, boolean quoteChanged) {}
    private record Owner(String owner, String description) {}
}
