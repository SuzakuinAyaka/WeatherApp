package com.dengyy.weatherapp.network.parser;

import android.text.TextUtils;

import com.dengyy.weatherapp.model.City;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CitySearchParser {

    public List<City> parse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        validateSuccess(root);

        JSONArray geocodes = root.optJSONArray("geocodes");
        List<City> results = new ArrayList<>();
        Set<String> seenAdCodes = new HashSet<>();
        if (geocodes == null) {
            return results;
        }

        for (int i = 0; i < geocodes.length(); i++) {
            JSONObject item = geocodes.optJSONObject(i);
            if (item == null) {
                continue;
            }

            String adCode = item.optString("adcode");
            if (TextUtils.isEmpty(adCode) || !seenAdCodes.add(adCode)) {
                continue;
            }

            String province = item.optString("province");
            String cityName = pickDisplayName(item, province);
            if (TextUtils.isEmpty(cityName)) {
                continue;
            }

            City city = new City();
            city.setCityName(cityName);
            city.setAdCode(adCode);
            city.setProvince(province);
            results.add(city);
        }
        return results;
    }

    private String pickDisplayName(JSONObject item, String province) {
        String city = readTextOrFirstArrayItem(item.opt("city"));
        String district = item.optString("district");
        if (!TextUtils.isEmpty(city)) {
            return city;
        }
        if (!TextUtils.isEmpty(district)) {
            return district;
        }
        return province;
    }

    private String readTextOrFirstArrayItem(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            if (array.length() > 0) {
                return array.optString(0);
            }
        }
        return "";
    }

    private void validateSuccess(JSONObject root) throws JSONException {
        String status = root.optString("status");
        if ("1".equals(status)) {
            return;
        }
        String info = root.optString("info");
        String infocode = root.optString("infocode");
        throw new JSONException(buildErrorMessage("AMap city search failed", info, infocode));
    }

    private String buildErrorMessage(String prefix, String info, String infocode) {
        if ("10009".equals(infocode)) {
            return prefix + ": " + info + " (" + infocode + "). Please use an AMap Web Service key and rebuild the app.";
        }
        return prefix + ": " + info + " (" + infocode + ")";
    }
}
