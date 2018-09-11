package com.zw.demo.action;

import com.zw.springframework.annotation.Autowired;
import com.zw.springframework.annotation.Controller;
import com.zw.springframework.annotation.RequestMapping;
import com.zw.springframework.annotation.RequestParam;
import com.zw.springframework.webmvc.ModelAndView;
import com.zw.demo.service.IQueryService;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@Controller
@RequestMapping("/")
public class PageAction {

	@Autowired
	IQueryService queryService;
	
	@RequestMapping("/first.html")
	public ModelAndView query(@RequestParam("teacher") String teacher){
		String result = queryService.query(teacher);
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("teacher", teacher);
		model.put("data", result);
		model.put("token", "123456");
		return new ModelAndView("first.html",model);
	}
	
}
