package com.quotes_museum.backend.services;

import com.quotes_museum.backend.models.quotes.QuotesDTO;
import com.quotes_museum.backend.models.quotes.QuotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@Component
@Service
public class QuotesService {

    @Autowired
    private final QuotesRepository quotesRepository;

    public List<String> getCoreTable(String table) throws SQLException {
        switch (table) {
            case "attrs" -> {
                return quotesRepository.getCoreTable(QuotesRepository.CoreTables.attrs);
            }
            case "features" -> {
                return quotesRepository.getCoreTable(QuotesRepository.CoreTables.features);
            }
            case "owners" -> {
                return quotesRepository.getCoreTable(QuotesRepository.CoreTables.owners);
            }
            default -> throw new UnsupportedOperationException("Dont know that type of table: " + table);
        }
    }

    public List<QuotesDTO> getfavQuotes(Principal principal) throws SQLException {

        return quotesRepository.getFavQuotes(principal.getName());
    }

    public List<QuotesDTO> getOwnerQuotes(String owner) throws SQLException {
        return quotesRepository.getOwnerQuotes(owner);
    }

    public boolean addFavouriteQuote(Principal principal, QuotesDTO quote) {

        return quotesRepository.addFavouriteQuote(principal.getName(), quote.getId());
    }

    public boolean insertQuote(QuotesDTO quote, Principal principal){

        return quotesRepository.insertQuote(quote, principal.getName());
    }

    public String updateQuote(QuotesDTO quote, boolean q, boolean a, boolean f, Principal principal) {

        if (!quotesRepository.isUpdatePermitted(quote, principal.getName())) return "not permitted to take others!";

        boolean quoteUpdate = true;
        boolean attrsUpdate = true;
        boolean featuresUpdate = true;

        if (q) quoteUpdate = quotesRepository.updateQuote(quote);
        if (a) attrsUpdate = quotesRepository.updateAttrs(quote);
        if (f) featuresUpdate = quotesRepository.updateFeatures(quote);

        String res = (quoteUpdate && attrsUpdate && featuresUpdate)? "Success!": "Failure:";

        if (!quoteUpdate) res += "\nin updating quotes";
        if (!attrsUpdate) res += "\nin updating attributes";
        if (!featuresUpdate) res += "\nin updating features";

        return res;
    }

    public boolean deleteQuote(int id, Principal principal) {
        if (!quotesRepository.isUpdatePermitted(id, principal.getName())) return false;

        return quotesRepository.deleteQuote(id);
    }

    public boolean addOwner(String owner, String description, Principal principal){
        return quotesRepository.addOwner(owner, description, principal.getName());
    }

    public List<QuotesDTO> searchQuote(String quote) throws SQLException {
        return quotesRepository.searchQuote(quote);
    }

}
