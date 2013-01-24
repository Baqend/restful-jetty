package info.orestes.rest;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class RestResponse extends HttpServletResponseWrapper {

	public RestResponse(HttpServletResponse response) {
		super(response);
	}

}
