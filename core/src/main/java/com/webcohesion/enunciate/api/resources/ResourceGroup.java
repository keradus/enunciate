package com.webcohesion.enunciate.api.resources;

import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface ResourceGroup {

  String getSlug();

  String getPath();

  String getDescription();

  String getDeprecated();

  Set<String> getMethods();

  List<Resource> getResources();

}