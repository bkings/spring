package com.java.broadwayProject.controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.java.broadwayProject.daos.CustomerDao;
import com.java.broadwayProject.daos.PointsDao;
import com.java.broadwayProject.models.Customer;
import com.java.broadwayProject.models.DateTime;
import com.java.broadwayProject.models.Points;

@Controller
public class LoginController {

	@Autowired
	private CustomerDao cdao;

	@Autowired
	private PointsDao pdao;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login() {
		return "login";
	}

	@RequestMapping(value = "/custlogin", method = RequestMethod.POST)
	public String customerlogin(@ModelAttribute Customer c, Model model, HttpSession session) {
		
		if (cdao.validLogin(c.getEmail(), c.getPassword())) {
			//session.setAttribute("customerEmail", c.getEmail());
			// session.setAttribute("customerInfo", c); //here c contains only
			// email and psw as a result of modelAttribute
			// session.setMaxInactiveInterval(10);

			int id = cdao.getId(c.getEmail(), c.getPassword());
			Customer customer = cdao.getById(id);
			session.setAttribute("customerInfo", customer);
			model.addAttribute("customer", customer.getFname());
			model.addAttribute("customerLname",customer.getLname());

			int countLogIns = customer.getNumberOflogInsPerDay();
			countLogIns++;
			customer.setNumberOflogInsPerDay(countLogIns);

			// -----------Date and Time------------------//
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			LocalDateTime currDateTime = getDateTime();
			String currentDateTime = currDateTime.format(dtf);
			
			//------------for Points----------------------//
			Points p = customer.getCustomerPoints();
			int checkAccumulation = p.getAccumulation();
			
			System.out.println(checkAccumulation);
			System.out.println(customer.getEmail());

			if (firstLogin(countLogIns)) { // checks if its user's first Login
				long difference=5;			//during first login,there is no previous reference date and time.
				DateTime dt = new DateTime(); // new object created because
												// initially datetime is null.
				dt.setLoginDateTime(currentDateTime);
				cdao.addDateTime(dt); // add for the first time
				customer.setDateTime(dt);
				int collection=addPoints(checkAccumulation); 		//add if first login
				p.setAccumulation(collection);
				pdao.update(p);
				session.setAttribute("dateTimeDifference", difference);		//due to if else condition this session is necessary.
				session.setAttribute("currentPoint", collection);			//to retrieve at home
			} else {

				// Check date and time
				DateTime dte = customer.getDateTime(); // to retrieve the corresponding id of the customer
				String dbDateTime = dte.getLoginDateTime();
				LocalDateTime dBDateTime = LocalDateTime.parse(dbDateTime, dtf);

				System.out.println(dbDateTime);
				System.out.println(dBDateTime);

				Duration duration = Duration.between(currDateTime, dBDateTime);

				System.out.println(duration);

				long difference = Math.abs(duration.toMinutes());
				System.out.println(difference);

				/*
				 * long difference=Math.abs(duration.toHours());
				 * System.out.println(difference);
				 */
				if (difference >= 5) {
					// access to points
					System.out.println("Access to points granted");
					dte.setLoginDateTime(currentDateTime);
					cdao.updateDateTime(dte); // after adding date and time just
												// udpdate those fields.
					int collection=addPoints(checkAccumulation);	//add only if 24 hr crossed
					System.out.println(collection);
					p.setAccumulation(collection);
					pdao.update(p);
					session.setAttribute("currentPoint", collection);

				} else {
					System.out.println("Go to hell");
					session.setAttribute("currentPoint", checkAccumulation);
				}
				
				customer.setDateTime(dte);
				model.addAttribute("datetimeDifference", difference);
				session.setAttribute("dateTimeDifference", difference);	
			}
			

			cdao.updateCustomerTable(customer);
			session.setAttribute("allCustomerInfo", customer);
			return "home";
		}
		
			return "login";
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String customerlogout(HttpSession session) {
		session.invalidate();
		return "login";
	}

	public boolean firstLogin(int countLogIns) {

		if (countLogIns == 1) {
			return true;
		}
		return false;
	}

	public LocalDateTime getDateTime() {
		LocalDateTime cdt = LocalDateTime.now();
		return cdt;
	}
	
	public int addPoints(int checkAccumulation){
		int collection = 50;
		if (checkAccumulation >= 10) {
			collection = collection + checkAccumulation;
		}
		return collection;
	}

}
