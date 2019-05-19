package com.modestie.modestieapp.model.character;

import android.util.Log;

import com.modestie.modestieapp.model.item.Materia;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GearItem
{
    private static final String TAG = "XIVAPI.CHA.GEARITEM";

    private int itemID;
    private String itemIcon;
    private int itemLevel;
    private int creatorID;
    private ArrayList<Materia> materias;

    private String dyeName;
    private int dyeID;
    private String dyeIcon;

    private String mirageName;
    private int mirageID;
    private String mirageIcon;

    public GearItem(JSONObject obj)
    {
        try
        {
            JSONObject item = obj.getJSONObject("Item");
            this.itemID = item.getInt("ID");
            this.itemIcon = item.getString("Icon");
            this.itemLevel = item.getInt("LevelItem");

            if(!obj.isNull("Creator"))
                this.creatorID = obj.getInt("Creator");

            if(!obj.isNull("Dye"))
            {
                JSONObject dye = obj.getJSONObject("Dye");
                this.dyeName = dye.getString("Name");
                this.dyeID = dye.getInt("ID");
                this.dyeIcon = dye.getString("Icon");
            }

            if(!obj.isNull("Mirage"))
            {
                JSONObject mirage = obj.getJSONObject("Mirage");
                this.mirageName = mirage.getString("Name");
                this.mirageID = mirage.getInt("ID");
                this.mirageIcon = mirage.getString("Icon");
            }

            this.materias = new ArrayList<>();
            if(!obj.isNull("Materia"))
            {
                JSONArray apiMaterias = obj.getJSONArray("Materia");
                for(int i = 0; i < apiMaterias.length(); i++)
                {
                    this.materias.add(new Materia(apiMaterias.getJSONObject(i)));
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public int getItemID()
    {
        return itemID;
    }

    public void setItemID(int itemID)
    {
        this.itemID = itemID;
    }

    public String getItemIcon()
    {
        return itemIcon;
    }

    public void setItemIcon(String itemIcon)
    {
        this.itemIcon = itemIcon;
    }

    public int getItemLevel()
    {
        return itemLevel;
    }

    public void setItemLevel(int itemLevel)
    {
        this.itemLevel = itemLevel;
    }

    public int getCreatorID()
    {
        return creatorID;
    }

    public void setCreatorID(int creatorID)
    {
        this.creatorID = creatorID;
    }

    public ArrayList<Materia> getMaterias()
    {
        return materias;
    }

    public void setMaterias(ArrayList<Materia> materias)
    {
        this.materias = materias;
    }

    public String getDyeName()
    {
        return dyeName;
    }

    public void setDyeName(String dyeName)
    {
        this.dyeName = dyeName;
    }

    public int getDyeID()
    {
        return dyeID;
    }

    public void setDyeID(int dyeID)
    {
        this.dyeID = dyeID;
    }

    public String getDyeIcon()
    {
        return dyeIcon;
    }

    public void setDyeIcon(String dyeIcon)
    {
        this.dyeIcon = dyeIcon;
    }

    public String getMirageName()
    {
        return mirageName;
    }

    public void setMirageName(String mirageName)
    {
        this.mirageName = mirageName;
    }

    public int getMirageID()
    {
        return mirageID;
    }

    public void setMirageID(int mirageID)
    {
        this.mirageID = mirageID;
    }

    public String getMirageIcon()
    {
        return mirageIcon;
    }

    public void setMirageIcon(String mirageIcon)
    {
        this.mirageIcon = mirageIcon;
    }
}
