package com.ponyvillesquare.serverbridge;

import java.net.InetAddress;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.maxmind.geoip.Country;

import uk.org.whoami.geoip.GeoIPLookup;
import uk.org.whoami.geoip.GeoIPTools;

public class GeoIPHook {
	private Optional<GeoIPLookup> geoiplookup;

	public GeoIPHook() {
		try {
			geoiplookup = Optional.<GeoIPLookup>ofNullable(JavaPlugin.getPlugin(GeoIPTools.class).getGeoIPLookup());
			if (!geoiplookup.isPresent())
				Log.log("Could not load GeoIPTools!", Level.SEVERE);
		} catch (final Exception e) {
			Log.log("An error occured loading GeoIPTools. Is the plugin loaded?", Level.SEVERE);
			e.printStackTrace();
		}
	}

	public Optional<String> getCountry(final InetAddress ip) {
		if (!geoiplookup.isPresent())
			return Optional.<String>empty();
		final Optional<Country> countryop = Optional.<Country>ofNullable(geoiplookup.get().getCountry(ip));
		String country = null;
		if (countryop.isPresent()) {
			country = countryop.get().getName();
			if (country.equals("N/A"))
				country = null;
			if (country.equals("Korea, Republic of"))
				country = "Republic of Korea";
			if (country.equals("Korea, Democratic People's Republic of"))
				country = "Democratic People's Republic of Korea";
		}
		return Optional.<String>ofNullable(country);
	}
}
