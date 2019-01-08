package io.jenkins.plugins.utils;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import hudson.util.Secret;

public class SecretSerializer implements JsonSerializer<Secret> {

  public JsonElement serialize(Secret secret, Type type,
    JsonSerializationContext jsonSerializationContext) {
	  return new JsonPrimitive(Secret.toString(secret));
  }
}