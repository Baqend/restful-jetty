package info.orestes.rest.service;

import info.orestes.rest.RestServlet;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.error.BadRequest;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.PathElement.Type;
import info.orestes.rest.util.Inject;
import info.orestes.rest.util.Module;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.UrlEncoded;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class RestRouter extends HandlerWrapper {

    public static final String REST_REQUEST = RestRequest.class.getName();
    public static final String REST_RESPONSE = RestResponse.class.getName();

	private final Module module;
	private final ConverterService converterService;
	private final List<RestMethod> methods = new ArrayList<>();
	private final ArrayList<ArrayList<Route>> routeLists = new ArrayList<>(10);
    private final List<Route> dynamicRoutes = new ArrayList<>(0);

	@Inject
	public RestRouter(Module module) {
		this.module = module;
		this.converterService = module.moduleInstance(ConverterService.class);
    }

	@Override
	public void handle(String path, Request request, HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
        RestRequest restRequest = (RestRequest) request.getAttribute(REST_REQUEST);
        RestResponse restResponse = (RestResponse) request.getAttribute(REST_RESPONSE);

        if (restRequest == null) {
			HttpURI uri = request.getHttpURI();

			// jetty decodes the path param
			path = uri.getPath();

            String contextPath = request.getContextPath();
			if (contextPath != null) {
				if (contextPath.endsWith("/"))
					contextPath = contextPath.substring(0, contextPath.length() - 1);

				path = path.substring(contextPath.length());
			}

			Map<String, String> matrix = null;
			int paramsIndex = path.indexOf(";");
			if (paramsIndex != -1) {
				matrix = createMap(path.substring(paramsIndex + 1).split(";"));
				path = path.substring(0, paramsIndex);
			}

			Map<String, String> query = uri.getQuery() == null ? null : createMap(uri.getQuery().split("&"));
			List<String> pathParts = null;
			try {
				pathParts = decodePath(path);
			} catch (BadRequest e) {
				res.sendError(400);
				request.setHandled(true);
				return;
			}

			for (Route route : getRoutes(pathParts.size())) {
				String method = request.getMethod();

				Map<String, String> matches = route.match(method, pathParts, matrix, query);
				if (matches != null) {
					if (!matches.isEmpty()) {
						//jetty use a constant Map in some cases therefore lets create always a new map
						MultiMap<String> params = request.getQueryParameters();
						params = params == null ? new MultiMap<>() : new MultiMap<>(params);
						params.putAllValues(matches);
						request.setQueryParameters(params);
					}

					restRequest = creatRequest(request, req, route);
					restResponse = createResponse(request, restRequest, res);

					request.setAttribute(REST_REQUEST, restRequest);
					request.setAttribute(REST_RESPONSE, restResponse);
					try {
						restRequest.setMatches(matches);
						break;
					} catch (RestException e) {
						restResponse.sendError(e);
						request.setHandled(true);
						return;
					}
				}
			}
		}

		if (restRequest != null) {
			super.handle(path, request, restRequest, restResponse);
			request.setHandled(true);
		}
	}

	protected List<String> decodePath(String path) throws BadRequest {
		try {
			List<String> pathParts = new ArrayList<>();
			int next;
			int offset = 1;
			while ((next = path.indexOf('/', offset)) != -1) {
				pathParts.add(URIUtil.decodePath(path, offset, next - offset));
				offset = next + 1;
			}
			pathParts.add(URIUtil.decodePath(path, offset, path.length() - offset));
			return pathParts;
		} catch (Exception e) {
			throw new BadRequest("Unsupported URI encoding", e);
		}
	}

	protected RestRequest creatRequest(Request baseRequest, HttpServletRequest req, Route route) {
		return new RestRequest(baseRequest, req, route, converterService);
	}

	protected RestResponse createResponse(Request baseRequest, RestRequest request, HttpServletResponse response) {
		return new RestResponse(request, response);
	}

	protected Map<String, String> createMap(String[] params) {
		Map<String, String> map = new HashMap<>();

		for (String str : params) {
			int index = str.indexOf('=');
			if (index == -1) {
				map.put(UrlEncoded.decodeString(str, 0, str.length(), null), null);
			} else {
				map.put(UrlEncoded.decodeString(str, 0, index, null),
					UrlEncoded.decodeString(str, index + 1, str.length() - index - 1, null));
			}
		}

		return map;
	}

	public List<RestMethod> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	// modified
	public void add(RestMethod method) {
		if (isStarted()) {
			throw new IllegalStateException("The router can not be modified while it is running");
		}

        //expand the routes table to the required path size
        int index = method.getFixedSignature().size() - 1;
        while (routeLists.size() <= index) {
            //copy all dynamic routes to all new routes since dynamic routes matches all paths of this size
            routeLists.add(new ArrayList<>(dynamicRoutes));
        }

        Route route = new Route(method);

        List<Route> routes = routeLists.get(index);
        routes.add(route);
        Collections.sort(routes);

        if (route.isDynamic()) {
            dynamicRoutes.add(route);
            Collections.sort(dynamicRoutes);

            //dynamic routes match all larger paths than this one,
            //therefore add this route to all larger path routing tables
            for (int i = index + 1; i < routeLists.size(); ++i) {
                ArrayList<Route> largerRoutes = routeLists.get(i);
                largerRoutes.add(route);
                Collections.sort(largerRoutes);
            }
        }

		methods.add(method);
	}

	public void addAll(Collection<RestMethod> methods) {
		for (RestMethod method : methods) {
			add(method);
		}
	}

	public void remove(RestMethod method) {
		if (isStarted()) {
			throw new IllegalStateException("The router can not be modified while it is running");
		}

        boolean removed = methods.remove(method);
        if (removed) {
            int index = method.getFixedSignature().size() - 1;
            List<Route> list = routeLists.get(index);
            Route route = null;
            for (Iterator<Route> iter = list.iterator(); iter.hasNext();) {
				route = iter.next();
				if (route.getMethod() == method) {
					iter.remove();
					break;
				}
			}

            if (route != null && route.isDynamic()) {
                //remove the dynamic route from all larger routing tables
                for (int i = index + 1; i < routeLists.size(); ++i) {
                    ArrayList<Route> largerRoutes = routeLists.get(i);
                    largerRoutes.remove(route);
                }
                dynamicRoutes.remove(route);
            }
		}
	}

	public void removeAll(Collection<RestMethod> methods) {
		for (RestMethod method : methods) {
			remove(method);
		}
	}

	public void clear() {
		if (isStarted()) {
			throw new IllegalStateException("The router can not be modified while it is running");
		}

		routeLists.clear();
        dynamicRoutes.clear();
		methods.clear();
	}

	protected List<Route> getRoutes(int parts) {
		int index = parts - 1;

        if (routeLists.size() <= index) {
			return dynamicRoutes;
		} else {
			return routeLists.get(index);
		}
	}

	public class Route implements Comparable<Route> {

		private final RestMethod method;
		private final RestServlet servlet;

		public Route(RestMethod method) {
			this.method = method;
            this.servlet = module.inject(method.getTarget());
		}

        public boolean isDynamic() {
            return method.hasDynamicPath();
        }

		public RestMethod getMethod() {
			return method;
		}

        public RestServlet getServlet() {
            return servlet;
        }

        public RestRouter getRouter() {
			return RestRouter.this;
		}

		@Override
		public int compareTo(Route o) {
			final List<Type> compareTypes = Arrays.asList(Type.PATH, Type.REGEX, Type.VARIABLE, Type.WILDCARD);
			List<PathElement> self = getMethod().getSignature();
			List<PathElement> other = o.getMethod().getSignature();

			int len = Math.min(self.size(), other.size());
			for (int i = 0; i < len; ++i) {
				Type selfType = self.get(i).getType();
				Type otherType = other.get(i).getType();

				if (selfType != otherType) {
					// check type order
					if (compareTypes.contains(selfType) || compareTypes.contains(otherType))
						return selfType.compareTo(otherType);
					else
						break;
				}
			}

			// if the path structure is identical the route with more required
			// parameters wins e.g. will be matched first
			return o.getMethod().getRequiredParamaters() - getMethod().getRequiredParamaters();
		}

		public Map<String, String> match(String action, List<String> pathParts, Map<String, String> matrix,
				Map<String, String> query) {
			switch (action) {
				case "OPTIONS":
					break;
				case "HEAD":
					action = "GET";
				default:
					if (!getMethod().getAction().equals(action)) {
						return null;
					}
			}

			Map<String, String> matches = new HashMap<>();

			int matrixCounter = matrix == null ? 0 : matrix.size();

			int parts = 0;
			for (PathElement el : getMethod().getSignature()) {
				switch (el.getType()) {
					case PATH: {
						// The matching path is longer or not equal to the
						// requested path
						if (pathParts.size() <= parts || !el.getName().equals(pathParts.get(parts++))) {
							return null;
						}
						break;
					}
					case VARIABLE: {
						// The matching path is longer than the requested path
						if (pathParts.size() <= parts) {
							return null;
						}

						String value = pathParts.get(parts++);
						if (value.isEmpty()) {
							return null;
						}

						matches.put(el.getName(), value);
						break;
					}
					case REGEX: {
						// The matching path is longer than the requested path
						if (pathParts.size() <= parts) {
							return null;
						}

						String value = pathParts.get(parts++);
						if (!el.getRegex().matcher(value).find()) {
							return null;
						}

						matches.put(el.getName(), value);
						break;
					}
					case WILDCARD: {
						// The matching path is longer than the requested path
						if (pathParts.size() <= parts) {
							return null;
						}

						//consume all remaining parts
						List<String> remainingParts = pathParts.subList(parts, pathParts.size());
						parts = pathParts.size();
						String value = String.join("/", remainingParts);
						matches.put(el.getName(), value);
						break;
					}
                    case MATRIX: {
						if (matrix != null && matrix.containsKey(el.getName())) {
							matches.put(el.getName(), matrix.get(el.getName()));
							matrixCounter--;
						} else if (el.isOptional()) {
							matches.put(el.getName(), el.getDefaultValue());
						} else {
							return null;
						}
						break;
					}
					case QUERY: {
						if (matrixCounter != 0) {
							return null;
						}

						if (query != null && query.containsKey(el.getName())) {
							matches.put(el.getName(), query.get(el.getName()));
						} else if (el.isOptional()) {
							matches.put(el.getName(), el.getDefaultValue());
						} else {
							return null;
						}
						break;
					}
				}
			}

			return matches;
		}

        @Override
        public String toString() {
            return method.toString();
        }
    }
}
