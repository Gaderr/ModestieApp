package com.modestie.modestieapp.model.item;

import android.util.Log;

import com.modestie.modestieapp.model.character.ItemBaseParam;

import org.json.JSONObject;

import java.util.ArrayList;

public class Item
{
    private static final String TAG = "XIVAPI.ITEM";

    private String name;
    private int ID;
    private String Icon;
    private int isUnique;
    private int isUntradable; //Personnel
    private int rarity;
    private ArrayList<ItemBaseParam> baseParams;
    private int materiaSlotCount;
    private int levelEquip;
    private int levelItem;
    private String classJobCategory; //"GUE PLD ...
    private String itemCategory; //"Torse"

    public Item(JSONObject obj)
    {
        try
        {
             this.name = obj.getString("Name");
             this.ID = obj.getInt("ID");
             this.Icon = obj.getString("Icon");
             this.isUnique = obj.getInt("IsUnique");
             this.isUntradable = obj.getInt("IsUntradable");
             this.rarity = obj.getInt("Rarity");
             this.baseParams = new ArrayList<>();
             for (int i = 0; i <= 5; i++)
             {
                 if(obj.getJSONObject("BaseParam" + i) != null)
                     this.baseParams.add(new ItemBaseParam(obj.getJSONObject("BaseParam" + i), obj.getInt("BaseParamValue" + i)));
             }
             this.levelEquip = obj.getInt("LevelEquip");
             this.levelItem = obj.getInt("LevelItem");
             this.classJobCategory = obj.getJSONObject("ClassJobCategory").getString("Name");
             this.itemCategory = obj.getJSONObject("ItemUICategory").getString("Name");
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getID()
    {
        return ID;
    }

    public void setID(int ID)
    {
        this.ID = ID;
    }

    public String getIcon()
    {
        return Icon;
    }

    public void setIcon(String icon)
    {
        Icon = icon;
    }

    public int getIsUnique()
    {
        return isUnique;
    }

    public void setIsUnique(int isUnique)
    {
        this.isUnique = isUnique;
    }

    public int getIsUntradable()
    {
        return isUntradable;
    }

    public void setIsUntradable(int isUntradable)
    {
        this.isUntradable = isUntradable;
    }

    public int getRarity()
    {
        return rarity;
    }

    public void setRarity(int rarity)
    {
        this.rarity = rarity;
    }

    public ArrayList<ItemBaseParam> getBaseParams()
    {
        return baseParams;
    }

    public void setBaseParams(ArrayList<ItemBaseParam> baseParams)
    {
        this.baseParams = baseParams;
    }

    public int getMateriaSlotCount()
    {
        return materiaSlotCount;
    }

    public void setMateriaSlotCount(int materiaSlotCount)
    {
        this.materiaSlotCount = materiaSlotCount;
    }

    public int getLevelEquip()
    {
        return levelEquip;
    }

    public void setLevelEquip(int levelEquip)
    {
        this.levelEquip = levelEquip;
    }

    public int getLevelItem()
    {
        return levelItem;
    }

    public void setLevelItem(int levelItem)
    {
        this.levelItem = levelItem;
    }

    public String getClassJobCategory()
    {
        return classJobCategory;
    }

    public void setClassJobCategory(String classJobCategory)
    {
        this.classJobCategory = classJobCategory;
    }

    public String getItemCategory()
    {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory)
    {
        this.itemCategory = itemCategory;
    }
}
