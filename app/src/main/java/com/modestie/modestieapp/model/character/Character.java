package com.modestie.modestieapp.model.character;

import org.json.JSONException;
import org.json.JSONObject;

public class Character
{
    private static final String TAG = "XIVAPI.CHA";

    String name;
    int ID;
    String race;
    String tribe;
    String title;
    int gender;
    String bio;
    String nameday;
    String avatarURL;
    String portraitURL;

    public Character(String name, int ID, String race, String tribe, String title, int gender, String bio, String nameday, String avatarURL, String portraitURL)
    {
        this.name = name;
        this.ID = ID;
        this.race = race;
        this.tribe = tribe;
        this.title = title;
        this.gender = gender;
        this.bio = bio;
        this.nameday = nameday;
        this.avatarURL = avatarURL;
        this.portraitURL = portraitURL;
    }

    public Character(JSONObject characterObject)
    {
        try
        {
            this.ID = characterObject.getInt("ID");
            this.name = characterObject.getString("Name");
            this.gender = characterObject.getInt("Gender");
            this.nameday = characterObject.getString("Nameday");
            this.race = characterObject.getJSONObject("Race").getString("Name");
            this.tribe = characterObject.getJSONObject("Tribe").getString("Name");
            this.title = characterObject.getJSONObject("Title").getString("Name");
            this.bio = characterObject.getString("Bio");
            this.avatarURL = characterObject.getString("Avatar");
            this.portraitURL = characterObject.getString("Portrait");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getName()
    {
        return name;
    }

    public int getID()
    {
        return ID;
    }

    public int getGender()
    {
        return gender;
    }

    public String getNameday()
    {
        return nameday;
    }

    public String getRace()
    {
        return race;
    }

    public String getTribe()
    {
        return tribe;
    }

    public String getTitle()
    {
        return title;
    }

    public String getBio()
    {
        return bio;
    }

    public String getAvatarURL()
    {
        return avatarURL;
    }

    public String getPortraitURL()
    {
        return portraitURL;
    }

    @Override
    public String toString()
    {
        return "Character{" +
                "name='" + name + '\'' +
                ", ID=" + ID +
                ", race='" + race + '\'' +
                ", tribe='" + tribe + '\'' +
                ", title='" + title + '\'' +
                ", gender=" + gender +
                ", bio='" + bio + '\'' +
                ", nameday='" + nameday + '\'' +
                ", avatarURL='" + avatarURL + '\'' +
                ", portraitURL='" + portraitURL + '\'' +
                '}';
    }
}
