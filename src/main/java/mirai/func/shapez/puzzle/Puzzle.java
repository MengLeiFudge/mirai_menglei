package shapez.puzzle;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import mirai.func.shapez.base.Building;
import mirai.func.shapez.base.Building.BuildingType;
import mirai.func.shapez.base.Item;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author MengLeiFudge
 */
@Data
public class Puzzle {
    private int w;
    private int h;
    private Building[] buildings = new Building[0];
    private int version;
    private ArrayList<BuildingType> excludedBuildings = new ArrayList<>();
    private double difficulty;
    private double averageTime;
    private int downloads;
    private String shortKey;
    private String author;
    private int completions;
    private int id;
    private boolean completed;
    private String title;
    private int likes;

    private Puzzle() {
    }

    public static Puzzle getPuzzleInstanceByJson(JSONObject obj) {
        if (obj.containsKey("error")) {
            return null;
        }
        Puzzle puzzle = new Puzzle();
        JSONObject game = obj.getJSONObject("game");
        //part1: game - bounds
        JSONObject bounds = game.getJSONObject("bounds");
        puzzle.w = bounds.getInteger("w");
        puzzle.h = bounds.getInteger("h");
        //part2: game - buildings
        JSONArray buildings = game.getJSONArray("buildings");
        puzzle.buildings = new Building[buildings.size()];
        for (int i = 0; i < buildings.size(); i++) {
            JSONObject building = buildings.getJSONObject(i);
            // typeStr: block, emitter, goal
            String typeStr = building.getString("type");
            BuildingType type = BuildingType.getBuildingTypeByStr(typeStr);
            Item item = null;
            if (type != BuildingType.BLOCK) {
                String itemStr = building.getString("item");
                item = Item.getItemByShortKey(itemStr);
            }
            JSONObject pos = building.getJSONObject("pos");
            // r: 0,90,180,-90
            int r = pos.getInteger("r");
            int x = puzzle.convertToNewX(pos.getInteger("x"));
            int y = puzzle.convertToNewY(pos.getInteger("y"));
            puzzle.buildings[i] = new Building(type, item, r, x, y);
        }
        //part3: game - version
        puzzle.version = game.getInteger("version");
        //part4: game - excludedBuildings
        if (game.containsKey("excludedBuildings")) {
            JSONArray excludedBuildings = game.getJSONArray("excludedBuildings");
            for (int i = 0; i < excludedBuildings.size(); i++) {
                String excludedBuilding = excludedBuildings.getString(i);
                switch (excludedBuilding) {
                    case "balancer": {
                        puzzle.excludedBuildings.add(BuildingType.SPLITTER_LEFT);
                        puzzle.excludedBuildings.add(BuildingType.SPLITTER_RIGHT);
                        break;
                    }
                    case "cutter": {
                        puzzle.excludedBuildings.add(BuildingType.CUTTER);
                        puzzle.excludedBuildings.add(BuildingType.CUTTER_QUAD);
                        break;
                    }
                    case "rotater": {
                        puzzle.excludedBuildings.add(BuildingType.ROTATER_CW);
                        puzzle.excludedBuildings.add(BuildingType.ROTATER_CCW);
                        puzzle.excludedBuildings.add(BuildingType.ROTATER_180);
                        break;
                    }
                    case "stacker":
                        puzzle.excludedBuildings.add(BuildingType.STACKER);
                        break;
                    case "mixer":
                        puzzle.excludedBuildings.add(BuildingType.MIXER);
                        break;
                    case "painter": {
                        puzzle.excludedBuildings.add(BuildingType.PAINTER);
                        puzzle.excludedBuildings.add(BuildingType.PAINTER_MIRROR);
                        puzzle.excludedBuildings.add(BuildingType.PAINTER_DOUBLE);
                        break;
                    }
                    case "trash":
                        puzzle.excludedBuildings.add(BuildingType.TRASH);
                        break;
                    case "belt": {
                        puzzle.excludedBuildings.add(BuildingType.BELT_STRAIGHT);
                        puzzle.excludedBuildings.add(BuildingType.BELT_LEFT);
                        puzzle.excludedBuildings.add(BuildingType.BELT_RIGHT);
                        break;
                    }
                    case "underground_belt": {
                        puzzle.excludedBuildings.add(BuildingType.TUNNEL1_ENTRY);
                        puzzle.excludedBuildings.add(BuildingType.TUNNEL1_EXIT);
                        puzzle.excludedBuildings.add(BuildingType.TUNNEL2_ENTRY);
                        puzzle.excludedBuildings.add(BuildingType.TUNNEL2_EXIT);
                        break;
                    }
                }
            }
        }
        //part5: meta
        JSONObject meta = obj.getJSONObject("meta");
        puzzle.difficulty = meta.get("difficulty") == null ? -1 : meta.getDouble("difficulty");
        puzzle.averageTime = meta.get("averageTime") == null ? -1 : meta.getDouble("averageTime");
        puzzle.downloads = meta.getInteger("downloads");
        puzzle.shortKey = meta.getString("shortKey");
        puzzle.author = meta.getString("author");
        puzzle.completions = meta.getInteger("completions");
        puzzle.id = meta.getInteger("id");
        puzzle.completed = meta.getBoolean("completed");
        puzzle.title = meta.getString("title");
        puzzle.likes = meta.getInteger("likes");
        return puzzle;
    }

    // region 坐标转换

    /*
    图片中心向右0.6，向下0.6，即为0,0位置
     */

    int getCenterX() {
        return (int) (w / 2.0 + 0.6);
    }

    int getCenterY() {
        return (int) (h / 2.0 - 0.6);
    }

    int convertToNewX(int x) {
        return getCenterX() + x;
    }

    int convertToNewY(int y) {
        return getCenterY() - y;
    }

    int convertToOriginX(int x) {
        return x - getCenterX();
    }

    int convertToOriginY(int y) {
        return getCenterY() - y;
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        JSONObject gameObj = new JSONObject();
        obj.put("game", gameObj);
        JSONArray buildingsArr = new JSONArray();
        gameObj.put("buildings", buildingsArr);
        for (Building building : buildings) {
            JSONObject buildingObj = new JSONObject();
            buildingsArr.add(buildingObj);
            if (building.getItem() != null) {
                buildingObj.put("item", building.getItem().getShortKey());
            }
            JSONObject pos = new JSONObject();
            buildingObj.put("pos", pos);
            pos.put("r", building.getR());
            pos.put("x", convertToOriginX(building.getX()));
            pos.put("y", convertToOriginY(building.getY()));
            buildingObj.put("type", building.getType().toString());
        }
        JSONObject boundsObj = new JSONObject();
        gameObj.put("bounds", boundsObj);
        boundsObj.put("w", w);
        boundsObj.put("h", h);
        gameObj.put("version", version);
        JSONArray excludedBuildingsArr = new JSONArray();
        gameObj.put("excludedBuildings", excludedBuildingsArr);
        Set<String> excludedBuildingsSet = new LinkedHashSet<>();
        for (BuildingType excludedBuilding : excludedBuildings) {
            switch (excludedBuilding) {
                case SPLITTER_LEFT:
                case SPLITTER_RIGHT:
                    excludedBuildingsSet.add("balancer");
                    break;
                case CUTTER:
                case CUTTER_QUAD:
                    excludedBuildingsSet.add("cutter");
                    break;
                case ROTATER_CW:
                case ROTATER_CCW:
                case ROTATER_180:
                    excludedBuildingsSet.add("rotater");
                    break;
                case STACKER:
                    excludedBuildingsSet.add("stacker");
                    break;
                case MIXER:
                    excludedBuildingsSet.add("mixer");
                    break;
                case PAINTER:
                case PAINTER_MIRROR:
                case PAINTER_DOUBLE:
                    excludedBuildingsSet.add("painter");
                    break;
                case TRASH:
                    excludedBuildingsSet.add("trash");
                    break;
                case BELT_STRAIGHT:
                case BELT_LEFT:
                case BELT_RIGHT:
                    excludedBuildingsSet.add("belt");
                    break;
                case TUNNEL1_ENTRY:
                case TUNNEL1_EXIT:
                case TUNNEL2_ENTRY:
                case TUNNEL2_EXIT:
                    excludedBuildingsSet.add("underground_belt");
                    break;
            }
        }
        excludedBuildingsArr.addAll(excludedBuildingsSet);
        JSONObject metaObj = new JSONObject();
        obj.put("meta", metaObj);
        if (difficulty != -1) {
            metaObj.put("difficulty", difficulty);
        }
        if (averageTime != -1) {
            metaObj.put("averageTime", averageTime);
        }
        metaObj.put("downloads", downloads);
        metaObj.put("shortKey", shortKey);
        metaObj.put("author", author);
        metaObj.put("completions", completions);
        metaObj.put("id", id);
        metaObj.put("completed", completed);
        metaObj.put("title", title);
        metaObj.put("likes", likes);
        return obj;
    }
}
