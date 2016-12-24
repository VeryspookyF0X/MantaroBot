package net.kodehawa.mantarobot.cmd;

import java.awt.Color;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantarobot.module.Callback;
import net.kodehawa.mantarobot.module.CommandType;
import net.kodehawa.mantarobot.module.Module;
import net.kodehawa.mantarobot.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Information module.
 * @author Yomura
 *
 */
public class Info extends Module {
	
	public Info()
	{
		this.registerCommands();
	}

	@Override
	public void registerCommands(){
		super.register("serverinfo", "Retrieves guild/server information.", new Callback() {
			@Override
			public void onCommand(String[] args, String content, MessageReceivedEvent event) {
				guild = event.getGuild();
				channel = event.getChannel();
				EmbedBuilder embed = new EmbedBuilder();
				StringBuilder sb = new StringBuilder();
				int i = 0;
				for(Role tc : guild.getRoles()) {
					i++;
					if (i <= 79) {
						if (!tc.getName().contains("everyone") && i != guild.getRoles().size() - 1) {
							sb.append(tc.getName()).append(", ");
						} else {
							sb.append(tc.getName()).append(".");
							break;
						}
					}
				}
				int online = 0;
				for(Member u : guild.getMembers()){
					if(!u.getOnlineStatus().equals(OnlineStatus.OFFLINE)){
						online++;
					}
				}
				embed.setColor(guild.getOwner().getColor())
						.setAuthor("Guild Information", null, guild.getIconUrl())
						.setColor(Color.orange)
						.setDescription("Guild information for server " + guild.getName())
						.setThumbnail(guild.getIconUrl())
						.addField("Users (Online/Unique)", online + "/" + guild.getMembers().size(), true)
						.addField("Main Channel", "#" + guild.getPublicChannel().getName(), true)
						.addField("Creation Date", guild.getCreationTime().format(DateTimeFormatter.ISO_DATE_TIME).replaceAll("[^0-9.:-]", " "), true)
						.addField("Voice/Text Channels", guild.getVoiceChannels().size() + "/" + guild.getTextChannels().size() , true)
						.addField("Owner", guild.getOwner().getUser().getName() + "#" + guild.getOwner().getUser().getDiscriminator(), true)
						.addField("Region", guild.getRegion().getName(), true)
						.addField("Roles ("+guild.getRoles().size() + ")", sb.toString(), false)
						.setFooter("Server ID: " + String.valueOf(guild.getId()), null);
				channel.sendMessage(embed.build()).queue();
			}

			@Override
			public String help() {
				return "Retrieves guild/server information. No need to use arguments.";
			}

			@Override
			public CommandType commandType() {
				return CommandType.USER;
			}
		});

		super.register("userinfo", "Retrieves user information.", new Callback() {
			@Override
			public void onCommand(String[] args, String content, MessageReceivedEvent event) {
				EmbedBuilder embed = new EmbedBuilder();
				guild = event.getGuild();
				channel = event.getChannel();
				receivedMessage = event.getMessage();
				author = event.getAuthor();
				if(!content.isEmpty())
				{
					User user1 = null;
					//Which user to get the info for?
					if(receivedMessage.getMentionedUsers() != null){
						user1 = receivedMessage.getMentionedUsers().get(0);
					}
					//Member gives way, way more info than User.
					Member member1 = guild.getMember(user1);

					if(user1 != null && member1 != null){
						//This is all done using embeds. It looks nicer and cleaner.
						embed.setColor(member1.getColor());
						//If we are dealing with the owner, mark him as owner on the title.
						if(member1.isOwner()){
							embed.setTitle("User info for " + user1.getName() + " (Server owner)");
						} else{
							//If not, just use the normal title.
							embed.setTitle("User info for " + user1.getName());
						}
						embed.setThumbnail(user1.getAvatarUrl())
								//Only get the date from the Join Date. Also replace that random Z because I'm not using time.
								.addField("Join Date: ", member1.getJoinDate().format(DateTimeFormatter.ISO_DATE_TIME).replaceAll("[^0-9.:-]", " "), false);
						if(member1.getVoiceState().getChannel() != null){
							embed.addField("Voice channel: ", member1.getVoiceState().getChannel().getName(), false);
						}
						if(guild.getMember(user1).getGame() != null){
							embed.addField("Playing: ", guild.getMember(user1).getGame().getName(), false);
						}
						embed.addField("Roles", String.valueOf(member1.getRoles().size()), true);
						//Getting the hex value of the RGB color assuming no alpha that is >16 in value is required.
						if(member1.getColor() != null){
							embed.addField("Color", "#"+Integer.toHexString(member1.getColor().getRGB()).substring(2).toUpperCase(), true);
						}
						embed.setFooter("User ID: " + user1.getId(), null);
						channel.sendMessage(embed.build()).queue();
					}

				}
				else {
					//If the author wants to get self info.
					User user1 = author;
					//From author id, get the Member, so I can fetch the info.
					Member member1 = guild.getMemberById(author.getId());

					//This is all done using embeds. It looks nicer and cleaner.
					embed.setColor(member1.getColor());
					//If we are dealing with the owner, mark him as owner on the title.
					if(member1.isOwner()){
						embed.setTitle("Self user info for " + user1.getName() + " (Server owner)");
					} else{
						//If not, just use the normal title.
						embed.setTitle("Self user info for " + user1.getName());
					}
					embed.setThumbnail(user1.getAvatarUrl());
					//Only get the date from the Join Date. Also replace that random Z because I'm not using time.
					embed.addField("Join Date: ", member1.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("[^0-9.:-]", " "), false);
					if(member1.getVoiceState().getChannel() != null){
						embed.addField("Voice channel: ", member1.getVoiceState().getChannel().getName(), false);
					}
					if(guild.getMember(user1).getGame() != null){
						embed.addField("Playing: ", guild.getMember(user1).getGame().getName(), false);
					}
					embed.addField("Roles", String.valueOf(member1.getRoles().size()), true);
					//Getting the hex value of the RGB color assuming no alpha that is >16 in value is required.
					if(!String.valueOf(member1.getColor().getRGB()).isEmpty()){
						embed.addField("Color", "#"+Integer.toHexString(member1.getColor().getRGB()).substring(2).toUpperCase(), true);
					}
					embed.setFooter("User ID: " + user1.getId(), null);
					channel.sendMessage(embed.build()).queue();
				}
			}

			@Override
			public String help() {
				return "Retrieves user information."
						+ "Usage: \n"
						+ "~>info user [@user]: Retrieves the specified user information.\n"
						+ "~>info user: Retrieves self user information.\n";
			}

			@Override
			public CommandType commandType() {
				return CommandType.USER;
			}
		});
		super.register("weather", "Displays forecast information", new Callback() {
			@Override
			public void onCommand(String[] args, String content, MessageReceivedEvent event) {
				channel = event.getChannel();
				EmbedBuilder embed = new EmbedBuilder();

				if(!content.isEmpty()){
					try {
						long start = System.currentTimeMillis();
						//Get a parsed JSON.
						String APP_ID = "e2abde2e6ca69e90a73ddb43199031de";
						JSONObject jObject = new JSONObject(Utils.instance().getObjectFromUrl("http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(content, "UTF-8") + "&appid="+ APP_ID, event));
						//Get the object as a array.
						JSONArray data = jObject.getJSONArray("weather");
						String status = null;
						for(int i = 0; i < data.length(); i++) {
							JSONObject entry = data.getJSONObject(i);
							status = entry.getString("main"); //Used for weather status.
						}

						JSONObject jMain = jObject.getJSONObject("main"); //Used for temperature and humidity.
						JSONObject jWind = jObject.getJSONObject("wind"); //Used for wind speed.
						JSONObject jClouds = jObject.getJSONObject("clouds"); //Used for cloudiness.
						JSONObject jsys = jObject.getJSONObject("sys"); //Used for countrycode.

						long end = System.currentTimeMillis() - start;

						String countryCode = jsys.getString("country").toLowerCase();

						Double temp = (double)jMain.get("temp"); //Temperature in Kelvin.
						int pressure = (int) jMain.get("pressure"); //Pressure in kPA.
						int hum = (int) jMain.get("humidity"); //Humidity in percentage.
						Double ws = (double) jWind.get("speed"); //Speed in m/h.
						int clness = (int) jClouds.get("all"); //Cloudiness in percentage.

						//Simple math formulas to convert from universal to metric and imperial.
						Double finalTemperatureCelcius = temp - 273.15; //Temperature in Celcius degrees.
						Double finalTemperatureFarnheit = temp * 9/5 - 459.67; //Temperature in Farnheit degrees.
						Double finalWindSpeedMetric = ws * 3.6; //wind speed in km/h.
						Double finalWindSpeedImperial = ws / 0.447046; //wind speed in mph.

						embed.setColor(Color.CYAN)
								.setTitle(":flag_" + countryCode + ":" + " Forecast information for " + content) //For which city
								.setDescription(status + " (" + clness + "% cloudiness)") //Clouds, sunny, etc and cloudiness.
								.addField("Temperature", finalTemperatureCelcius.intValue() + "°C/" + finalTemperatureFarnheit.intValue() + "°F", true)
								.addField("Humidity", hum + "%" , true)
								.addBlankField(true)
								.addField("Wind Speed", finalWindSpeedMetric.intValue() + "kmh / " + finalWindSpeedImperial.intValue() + "mph" , true)
								.addField("Pressure", pressure + "kPA" , true)
								.addBlankField(true)
								.setFooter("Information provided by OpenWeatherMap (Process time: " + end + "ms)", null);
						//Build the embed message and send it.
						channel.sendMessage(embed.build()).queue();
					}
					catch(Exception e){
						e.printStackTrace();
					}
				} else {
					channel.sendMessage(help()).queue();
				}
			}

			@Override
			public String help() {
				return  "This command retrieves information from OpenWeatherMap. Used to check **forecast information.**\n"
						+ "> Usage:\n"
						+ "~>weather [city],[countrycode]: Retrieves the forecast information for such location.\n"
						+ "> Parameters:\n"
						+ "[city]: Your city name, for example New York\n"
						+ "[countrycode]: (OPTIONAL) The code for your country, for example US (USA) or MX (Mexico).";
			}

			@Override
			public CommandType commandType() {
				return CommandType.USER;
			}
		});
	}
}