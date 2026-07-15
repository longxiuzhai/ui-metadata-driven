package com.example.metadataui.card.model;
import java.util.List; import java.util.Map;
public final class CardDefinition {
 private final String code,title,component,dataApi,loadStrategy,permission; private final int order; private final ResponsiveSpan span; private final Map<String,Object> props; private final List<CardAction> actions;
 public CardDefinition(String code,String title,String component,int order,ResponsiveSpan span,String dataApi,String loadStrategy,String permission,Map<String,Object> props,List<CardAction> actions){this.code=code;this.title=title;this.component=component;this.order=order;this.span=span;this.dataApi=dataApi;this.loadStrategy=loadStrategy;this.permission=permission;this.props=props;this.actions=actions;}
 public String getCode(){return code;} public String getTitle(){return title;} public String getComponent(){return component;} public int getOrder(){return order;} public ResponsiveSpan getSpan(){return span;} public String getDataApi(){return dataApi;} public String getLoadStrategy(){return loadStrategy;} public String getPermission(){return permission;} public Map<String,Object> getProps(){return props;} public List<CardAction> getActions(){return actions;}
}
