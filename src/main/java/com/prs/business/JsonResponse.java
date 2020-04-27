package com.prs.business;

import java.util.ArrayList;

public class JsonResponse {

	private Object data = null;
	private Object errors = null;
	private Object meta = null;
	
	public JsonResponse(Object d) {
		data = d;
		meta = new ArrayList<>();
	}
	
	public JsonResponse(Exception e) {
		errors = e;
	}
	
	public JsonResponse(String s, boolean success) {
		if (success)
			data = s;
		else
			errors = s;
	}
	
	public Object getData() {
		return data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
	public Object getErrors() {
		return errors;
	}
	
	public void setErrors(Object errors) {
		this.errors = errors;
	}
	
	public Object getMeta() {
		return meta;
	}
	
	public void setMeta(Object meta) {
		this.meta = meta;
	}
	
	public static JsonResponse getInstance(Object d) {
		return new JsonResponse(d);
	}

	public static JsonResponse getInstance(Exception e) {
		return new JsonResponse(e.getMessage());
	}
	public static JsonResponse getErrorInstance(String msg) {
		return new JsonResponse(msg, false);
	}

	public static JsonResponse getInstance(String msg) {
		return new JsonResponse(msg, true);
	}
}