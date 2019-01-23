package com.java.broadwayProject.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CustomerController {

	@RequestMapping(value="/edit",method=RequestMethod.GET)
	public String editInfo(HttpSession session){
		if(session.getAttribute("allCustomerInfo")==null){
			return "login";
		}
		return "edit";
	}
	
}
