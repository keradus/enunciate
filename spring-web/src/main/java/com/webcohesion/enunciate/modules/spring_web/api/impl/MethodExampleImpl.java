package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Example;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.modules.spring_web.model.*;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class MethodExampleImpl implements Example {

  private final String httpMethod;
  private final RequestMapping resourceMethod;
  private final MediaTypeDescriptor requestDescriptor;
  private final MediaTypeDescriptor responseDescriptor;

  public MethodExampleImpl(String httpMethod, RequestMapping resourceMethod, ApiRegistrationContext registrationContext) {
    this.httpMethod = httpMethod;
    this.resourceMethod = resourceMethod;

    MediaTypeDescriptor requestDescriptor = null; //try to find a request example.
    ResourceEntityParameter entityParameter = this.resourceMethod.getEntityParameter();
    if (entityParameter != null) {
      RequestEntityImpl entity = new RequestEntityImpl(this.resourceMethod, entityParameter, registrationContext);
      List<? extends MediaTypeDescriptor> mediaTypes = entity.getMediaTypes();
      Collections.sort(mediaTypes, new Comparator<MediaTypeDescriptor>() {
        @Override
        public int compare(MediaTypeDescriptor d1, MediaTypeDescriptor d2) {
          return new Float(d2.getQualityOfSourceFactor()).compareTo(d1.getQualityOfSourceFactor());
        }
      });
      requestDescriptor = mediaTypes.isEmpty() ? null : mediaTypes.get(0);
    }
    this.requestDescriptor = requestDescriptor;

    MediaTypeDescriptor responseDescriptor = null; //try to find a response example.
    ResourceRepresentationMetadata representationMetadata = this.resourceMethod.getRepresentationMetadata();
    if (representationMetadata != null) {
      ResponseEntityImpl entity = new ResponseEntityImpl(this.resourceMethod, representationMetadata, registrationContext);
      List<? extends MediaTypeDescriptor> mediaTypes = entity.getMediaTypes();
      Collections.sort(mediaTypes, new Comparator<MediaTypeDescriptor>() {
        @Override
        public int compare(MediaTypeDescriptor d1, MediaTypeDescriptor d2) {
          return new Float(d2.getQualityOfSourceFactor()).compareTo(d1.getQualityOfSourceFactor());
        }
      });
      responseDescriptor = mediaTypes.isEmpty() ? null : mediaTypes.get(0);
    }
    this.responseDescriptor = responseDescriptor;

  }

  @Override
  public String getRequestLang() {
    if (this.requestDescriptor == null) {
      return null;
    }

    String lang = "txt";
    com.webcohesion.enunciate.api.datatype.Example example = this.requestDescriptor.getExample();
    if (example != null) {
      lang = example.getLang();
    }

    return lang;
  }

  @Override
  public String getRequestHeaders() {
    String fullpath = this.resourceMethod.getFullpath();
    JavaDoc.JavaDocTagList pathExample = this.resourceMethod.getJavaDoc().get("pathExample");
    if (pathExample != null && !pathExample.isEmpty()) {
      fullpath = pathExample.get(0);
    }

    StringBuilder builder = new StringBuilder(this.httpMethod).append(' ').append(fullpath).append("\n");
    if (this.requestDescriptor != null) {
      builder.append("Content-Type: ").append(this.requestDescriptor.getMediaType()).append("\n");
    }
    if (this.responseDescriptor != null) {
      builder.append("Accept: ").append(this.responseDescriptor.getMediaType()).append("\n");
    }
    Set<RequestParameter> resourceParameters = this.resourceMethod.getRequestParameters();
    for (RequestParameter resourceParameter : resourceParameters) {
      if ("header".equalsIgnoreCase(resourceParameter.getTypeName())) {
        if ("content-type".equalsIgnoreCase(resourceParameter.getParameterName()) && this.requestDescriptor != null) {
          continue;
        }

        if ("accept".equalsIgnoreCase(resourceParameter.getParameterName()) && this.responseDescriptor != null) {
          continue;
        }

        String exampleValue = resourceParameter.getDefaultValue() != null ? resourceParameter.getDefaultValue() : "...";
        DocumentationExample documentationExample = resourceParameter.getAnnotation(DocumentationExample.class);
        if (documentationExample != null) {
          if (documentationExample.exclude()) {
            continue;
          }

          exampleValue = documentationExample.value();
        }
        builder.append(resourceParameter.getParameterName()).append(": ").append(exampleValue).append('\n');
      }
    }
    return builder.toString();
  }

  @Override
  public String getRequestBody() {
    if (this.requestDescriptor == null) {
      return null;
    }

    String body = "...";
    com.webcohesion.enunciate.api.datatype.Example example = this.requestDescriptor.getExample();
    if (example != null) {
      body = example.getBody();
    }

    return body;
  }

  @Override
  public String getResponseLang() {
    if (this.responseDescriptor == null) {
      return null;
    }

    String lang = "txt";
    com.webcohesion.enunciate.api.datatype.Example example = this.responseDescriptor.getExample();
    if (example != null) {
      lang = example.getLang();
    }

    return lang;
  }

  @Override
  public String getResponseHeaders() {
    int responseCode = "POST".equalsIgnoreCase(this.httpMethod) ? 201 : "PUT".equalsIgnoreCase(this.httpMethod) ? 204 : "DELETE".equalsIgnoreCase(this.httpMethod) ? 204 : 200;

    List<? extends ResponseCode> statusCodes = this.resourceMethod.getStatusCodes();
    if (statusCodes != null && !statusCodes.isEmpty()) {
      for (ResponseCode code : statusCodes) {
        if (code.getCode() >= 200 && code.getCode() < 400) {
          responseCode = code.getCode();
          break;
        }
      }
    }

    String message;
    try {
      message = HttpStatus.valueOf(responseCode).getReasonPhrase();
    }
    catch (IllegalArgumentException e) {
      message = "Custom Message";
    }

    StringBuilder builder = new StringBuilder("HTTP/1.1 ").append(responseCode).append(' ').append(message).append("\n");
    if (this.responseDescriptor != null) {
      builder.append("Content-Type: ").append(this.responseDescriptor.getMediaType()).append("\n");
    }

    for (String responseHeader : this.resourceMethod.getResponseHeaders().keySet()) {
      if ("content-type".equalsIgnoreCase(responseHeader) && this.responseDescriptor != null) {
        continue;
      }

      builder.append(responseHeader).append(": ").append("...").append("\n");
    }

    return builder.toString();
  }

  @Override
  public String getResponseBody() {
    if (this.responseDescriptor == null) {
      return null;
    }

    String body = "...";
    com.webcohesion.enunciate.api.datatype.Example example = this.responseDescriptor.getExample();
    if (example != null) {
      body = example.getBody();
    }

    return body;
  }
}
