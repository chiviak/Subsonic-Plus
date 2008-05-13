package net.sourceforge.subsonic.controller;

import net.sourceforge.subsonic.domain.InternetRadio;
import net.sourceforge.subsonic.service.SettingsService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the page used to administrate the set of internet radio/tv stations.
 *
 * @author Sindre Mehus
 */
public class InternetRadioSettingsController extends ParameterizableViewController {

    private SettingsService settingsService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        if (isFormSubmission(request)) {
            String error = handleParameters(request);
            map.put("error", error);
            if (error == null) {
                map.put("reload", true);
            }
        }

        ModelAndView result = super.handleRequestInternal(request, response);
        map.put("internetRadios", settingsService.getAllInternetRadios(true));

        result.addObject("model", map);
        return result;
    }

    /**
     * Determine if the given request represents a form submission.
     *
     * @param request current HTTP request
     * @return if the request represents a form submission
     */
    private boolean isFormSubmission(HttpServletRequest request) {
        return "POST".equals(request.getMethod());
    }

    private String handleParameters(HttpServletRequest request) {
        InternetRadio[] radios = settingsService.getAllInternetRadios(true);
        for (InternetRadio radio : radios) {
            Integer id = radio.getId();
            String streamUrl = getParameter(request, "streamUrl", id);
            String homepageUrl = getParameter(request, "homepageUrl", id);
            String name = getParameter(request, "name", id);
            boolean enabled = getParameter(request, "enabled", id) != null;
            boolean delete = getParameter(request, "delete", id) != null;

            if (delete) {
                settingsService.deleteInternetRadio(id);
            } else {
                if (StringUtils.isBlank(name)) {
                    return "internetradiosettings.noname";
                }
                if (StringUtils.isBlank(streamUrl)) {
                    return "internetradiosettings.nourl";
                }
                settingsService.updateInternetRadio(new InternetRadio(id, name, streamUrl, homepageUrl, enabled));
            }
        }

        String name = request.getParameter("name");
        String streamUrl = request.getParameter("streamUrl");
        String homepageUrl = request.getParameter("homepageUrl");
        boolean enabled = request.getParameter("enabled") != null;

        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(streamUrl)) {
            settingsService.createInternetRadio(new InternetRadio(name, streamUrl, homepageUrl, enabled));
        }

        return null;
    }

    private String getParameter(HttpServletRequest request, String name, Integer radioId) {
        return request.getParameter(name + "[" + radioId + "]");
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

}
