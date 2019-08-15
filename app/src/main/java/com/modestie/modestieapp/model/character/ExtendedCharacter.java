package com.modestie.modestieapp.model.character;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExtendedCharacter extends Character
{
    private static final String TAG = "XIVAPI.CHA";

    public static final String[] GEAR_ITEM_KEYS =
            {
                    "MainHand",
                    "Head",
                    "Body",
                    "Hands",
                    "Waist",
                    "Legs",
                    "Feet",
                    "OffHand",
                    "Earrings",
                    "Necklace",
                    "Bracelets",
                    "Ring1",
                    "Ring2",
                    "SoulCrystal"
            };

    private boolean loaded;

    private ClassJob activeClassJob;
    private ArrayList<ClassJob> classJobs;
    private Map<String, GearItem> gearItems;

    private int ilvl;

    private ArrayList<CharacterAttribute> attributes;

    private CharacterGrandCompany grandCompany;

    private CharacterGuardianDeity guardianDeity;

    public ExtendedCharacter(JSONObject obj)
    {
        super(obj);

        this.loaded = false;

        try
        {
            this.activeClassJob = new ClassJob(obj.getJSONObject("ActiveClassJob"));

            this.classJobs = new ArrayList<>();
            JSONArray apiClassJobs = obj.getJSONArray("ClassJobs");

            for(int i = 0; i < apiClassJobs.length(); i++)
            {
                this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(i)));
            }

            JSONObject apiGearSet = obj.getJSONObject("GearSet");
            JSONObject apiGear = apiGearSet.getJSONObject("Gear");

            this.gearItems = new HashMap<>();
            for (String gearItemKey : GEAR_ITEM_KEYS)
            {
                if (!apiGear.isNull(gearItemKey))
                    this.gearItems.put(gearItemKey, new GearItem(apiGear.getJSONObject(gearItemKey)));
                else
                    this.gearItems.put(gearItemKey, null);
            }

            calcIlvl();

            JSONArray apiAttributes = apiGearSet.getJSONArray("Attributes");
            this.attributes = new ArrayList<>();
            for(int i = 0; i < apiAttributes.length(); i++)
            {
                this.attributes.add(new CharacterAttribute(apiAttributes.getJSONObject(i)));
            }

            this.grandCompany = new CharacterGrandCompany(obj.getJSONObject("GrandCompany"));

            this.guardianDeity = new CharacterGuardianDeity(obj.getJSONObject("GuardianDeity"));

            this.loaded = true;
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    private void calcIlvl()
    {
        int sum = 0;

        if(gearItems.get("OffHand") == null)
            sum += gearItems.get("MainHand").getItemLevel() * 2;
        else
            sum += gearItems.get("MainHand").getItemLevel() +
                    gearItems.get("OffHand").getItemLevel();

        for (String gearItemKey : GEAR_ITEM_KEYS)
        {
            if (!gearItemKey.equals("MainHand") && !gearItemKey.equals("OffHand") && !gearItemKey.equals("SoulCrystal"))
            {
                if(gearItems.get(gearItemKey) != null)
                    sum += gearItems.get(gearItemKey).getItemLevel();
            }
            else
            {
                Log.e(TAG, "Ignoring " + gearItemKey);
            }
        }

        this.ilvl = sum / 13;

        Log.e(TAG, this.ilvl + "");
    }

    public boolean isLoaded()
    {
        return loaded;
    }

    public static String[] getGearItemKeys()
    {
        return GEAR_ITEM_KEYS;
    }

    public ClassJob getActiveClassJob()
    {
        return activeClassJob;
    }

    public void setActiveClassJob(ClassJob activeClassJob)
    {
        this.activeClassJob = activeClassJob;
    }

    public ArrayList<ClassJob> getClassJobs()
    {
        return classJobs;
    }

    public void setClassJobs(ArrayList<ClassJob> classJobs)
    {
        this.classJobs = classJobs;
    }

    public Map<String, GearItem> getGearItems()
    {
        return gearItems;
    }

    public void setGearItems(Map<String, GearItem> gearItems)
    {
        this.gearItems = gearItems;
    }

    public int getIlvl()
    {
        return ilvl;
    }

    public void setIlvl(int ilvl)
    {
        this.ilvl = ilvl;
    }

    public ArrayList<CharacterAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(ArrayList<CharacterAttribute> attributes)
    {
        this.attributes = attributes;
    }

    public CharacterGrandCompany getGrandCompany()
    {
        return grandCompany;
    }

    public void setGrandCompany(CharacterGrandCompany grandCompany)
    {
        this.grandCompany = grandCompany;
    }

    public CharacterGuardianDeity getGuardianDeity()
    {
        return guardianDeity;
    }

    public void setGuardianDeity(CharacterGuardianDeity guardianDeity)
    {
        this.guardianDeity = guardianDeity;
    }
}
