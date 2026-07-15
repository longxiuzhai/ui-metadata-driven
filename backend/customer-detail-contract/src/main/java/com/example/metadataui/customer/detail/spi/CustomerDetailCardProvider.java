package com.example.metadataui.customer.detail.spi;
import com.example.metadataui.card.model.CardDefinition;
public interface CustomerDetailCardProvider { String cardCode(); default boolean supports(CustomerDetailContext context){return true;} CardDefinition definition(CustomerDetailContext context); }
