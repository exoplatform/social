/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.router;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.router.regex.ExoMatcher;
import org.exoplatform.social.common.router.regex.ExoPattern;
import org.picocontainer.Startable;

public class ExoRouter implements Startable {

  /**
   * The Logger.
   */
  private static final Log LOG = ExoLogger.getLogger(ExoRouter.class);
  
  static ExoPattern defaultRoutePattern = ExoPattern.compile("^({path}.*/[^\\s]*)\\s+({action}[^\\s(]+)({params}.+)?(\\s*)$");

  /**
   * All the loaded routes.
   */
  public static List<Route> routes = new CopyOnWriteArrayList<Route>();

  private ExoRouterConfig routerConfig;
  
  public static void reset() {
    routes.clear();
  }
  
  public ExoRouter() {}

  public void addRoutes(ExoRouterConfig routeConfig) {
    this.routerConfig = routeConfig;
    Map<String, String> routeMapping = this.routerConfig.getRouteMapping();
    
    for(Map.Entry<String, String> entry : routeMapping.entrySet()) {
      addRoute(entry.getValue(), entry.getKey());
    }
  }
  
  /**
   * Add new route which loaded from route configuration file.
   * 
   * @param path /{pageID}/ForumService
   * @param action the action which appends to patch after "ForumService"
   *          string.
   */
  public static void addRoute(String path, String action) {
    addRoute(path, action, null);
  }

  /**
   * Add new route which loaded from route configuration file.
   * 
   * @param path /{pageID}/ForumService
   * @param action /{pageID}/ForumService
   * @param params the action which appends to patch after "ForumService" string
   *          ex: /{pageID}/{ForumService|}/{action} =>/{pageID}/ForumService/{}
   */
  public static void addRoute(String path, String action, String params) {
    appendRoute(path, action, params);
  }

  public static void appendRoute(String path, String action, String params) {
    int position = routes.size();
    routes.add(position, getRoute(path, action, params));
  }

  public static Route getRoute(String path, String action, String params) {
    return getRoute(path, action, params, null, 0);
  }

  public static Route getRoute(String path, String action) {
    return getRoute(path, action, null, null, 0);
  }

  public static Route getRoute(String path, String action, String params, String sourceFile, int line) {
    Route route = new Route();
    route.path = path.replace("//", "/");
    route.action = action;
    route.routesFile = sourceFile;
    route.routesFileLine = line;
    route.addParams(params);
    route.compute();
    return route;
  }

  /**
   * Add a new route at the beginning of the route list
   */
  public static void prependRoute(String path, String action, String params) {
    routes.add(0, getRoute(path, action, params));
  }

  /**
   * Add a new route at the beginning of the route list
   */
  public static void prependRoute(String path, String action) {
    routes.add(0, getRoute(path, action));
  }

  public static Route route(String path) {
    for (Route route : routes) {
      Map<String, String> args = route.matches(path);
      if (args != null) {
        route.localArgs = args;
        return route;
      }
    }

    return null;
  }

  /**
   * Generates ActionBuilder base on the action name and arguments list.
   * Example: invokes reverse("show.topic", new HashMap<String, Object>{topicId, "topicId321"}) method. 
   *           
   * @param action
   * @param args
   * @return
   */
  public static ActionBuilder reverse(String action, Map<String, Object> args) {
    Map<String, Object> argsbackup = new HashMap<String, Object>(args);
    // Add routeArgs
    for (Route route : routes) {
      if (route.actionPattern != null) {
        ExoMatcher matcher = route.actionPattern.matcher(action);
        if (matcher.matches()) {
          for (String group : route.actionArgs) {
            String v = matcher.group(group);
            if (v == null) {
              continue;
            }
            args.put(group, v.toLowerCase());
          }
          List<String> inPathArgs = new ArrayList<String>(16);
          boolean allRequiredArgsAreHere = true;

          for (Route.ParamArg arg : route.args) {
            inPathArgs.add(arg.name);
            Object value = args.get(arg.name);
            if (value != null) {
              if (!value.toString().startsWith(":") && !arg.constraint.matches(value.toString())) {
                allRequiredArgsAreHere = false;
                break;
              }
            }
          }
          if (allRequiredArgsAreHere) {
            StringBuilder queryString = new StringBuilder();
            String path = route.path;
            if (path.endsWith("/?")) {
              path = path.substring(0, path.length() - 2);
            }
            for (Map.Entry<String, Object> entry : args.entrySet()) {
              String key = entry.getKey();
              Object value = entry.getValue();
              if (inPathArgs.contains(key) && value != null) {
                path = path.replaceAll("\\{(<[^>]+>)?" + key + "\\}", value.toString().replace("$", "\\$").replace("%3A", ":").replace("%40", "@"));
              } else if (value != null) {
                try {
                  queryString.append(URLEncoder.encode(key, "UTF-8"));
                  queryString.append("=");
                  if (value.toString().startsWith(":")) {
                    queryString.append(value.toString());
                  } else {
                    queryString.append(URLEncoder.encode(value.toString() + "", "UTF-8"));
                  }
                  queryString.append("&");
                } catch (UnsupportedEncodingException ex) {
                  LOG.debug("Unsupported encoding error: " + ex);
                }

              }
            }
            String qs = queryString.toString();
            if (qs.endsWith("&")) {
              qs = qs.substring(0, qs.length() - 1);
            }
            ActionBuilder actionDefinition = new ActionBuilder();
            actionDefinition.url = qs.length() == 0 ? path : path + "?" + qs;
            actionDefinition.action = action;
            actionDefinition.args = argsbackup;
            return actionDefinition;
          }
        }
      }
    }
    return null;
  }

  public static class ActionBuilder {
    public String url;

    /**
     * @todo - what is this? does it include the class and package?
     */
    public String action;

    /**
     * @todo - are these the required args in the routing file, or the query
     *       string in a request?
     */
    public Map<String, Object> args;

    public ActionBuilder add(String key, Object value) {
      args.put(key, value);
      return reverse(action, args);
    }

    public ActionBuilder remove(String key) {
      args.remove(key);
      return reverse(action, args);
    }

    public ActionBuilder addRef(String fragment) {
      url += "#" + fragment;
      return this;
    }

    @Override
    public String toString() {
      return url;
    }

  }
  
  /**
   * Route class which contains path, action & argument list.
   * 
   * @author thanhvc
   *
   */
  public static class Route {

    public String path;

    public String action;

    ExoPattern actionPattern;

    List<String> actionArgs = new ArrayList<String>(3);

    ExoPattern pattern;

    public String routesFile;

    List<ParamArg> args = new ArrayList<ParamArg>(3);

    Map<String, String> staticArgs = new HashMap<String, String>(3);

    public Map<String, String> localArgs = null;

    public int routesFileLine;

    static ExoPattern customRegexPattern =  ExoPattern.compile("\\{([a-zA-Z_][a-zA-Z_0-9]*)\\}");

    static ExoPattern argsPattern = ExoPattern.compile("\\{<([^>]+)>([a-zA-Z_0-9]+)\\}");

    static ExoPattern paramPattern = ExoPattern.compile("([a-zA-Z_0-9]+):'(.*)'");

    public void compute() {
      String patternString = this.path;
      patternString = customRegexPattern.matcher(patternString).replaceAll("\\{<[^/]+>$1\\}");
      ExoMatcher matcher = argsPattern.matcher(patternString);
      while (matcher.find()) {
        ParamArg arg = new ParamArg();
        arg.name = matcher.group(2);
        arg.constraint = ExoPattern.compile(matcher.group(1));
        args.add(arg);
      }

      patternString = argsPattern.matcher(patternString).replaceAll("({$2}$1)");
      this.pattern = ExoPattern.compile(patternString);
      // Action pattern
      patternString = action;
      patternString = patternString.replace(".", "[.]");
      for (ParamArg arg : args) {
        if (patternString.contains("{" + arg.name + "}")) {
          patternString = patternString.replace("{" + arg.name + "}", "({" + arg.name + "}" + arg.constraint.toString() + ")");
          actionArgs.add(arg.name);
        }
      }
      actionPattern = ExoPattern.compile(patternString, Pattern.CASE_INSENSITIVE);
    }

    public void addParams(String params) {
      if (params == null || params.length() < 1) {
        return;
      }
      params = params.substring(1, params.length() - 1);
      for (String param : params.split(",")) {
        ExoMatcher matcher = paramPattern.matcher(param);
        if (matcher.matches()) {
          staticArgs.put(matcher.group(1), matcher.group(2));
        } else {
          LOG.warn("Ignoring %s (static params must be specified as key:'value',...)");
        }
      }
    }
    /**
     * Base on defined Pattern, when provided URI path, 
     * this method will extract all of parameters path value 
     * in given path which reflects in defined Pattern
     * 
     * Example: 
     * defined Pattern = "/{pageID}/topic/{topicID}"
     * invokes:: matches("1256/topic/topic544343");
     * result: Map<String, String> = {"pageID" -> "1256"}, {"topicID" -> "topic544343"}
     * 
     * @param path : given URI path
     * @return
     */
    public Map<String, String> matches(String path) {
      ExoMatcher matcher = pattern.matcher(path);
      if (matcher.matches()) {
        Map<String, String> localArgs = new HashMap<String, String>();
        for (ParamArg arg : args) {
          if (arg.defaultValue == null) {
            localArgs.put(arg.name, matcher.group(arg.name));
          }
        }
        return localArgs;
      }

      return null;
    }

    static class ParamArg {
      String name;

      ExoPattern constraint;

      String defaultValue;
    }
  }

  @Override
  public void start() {
    
  }

  @Override
  public void stop() {
    
  }

}
