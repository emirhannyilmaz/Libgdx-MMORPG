package com.codemir.multiplayer.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codemir.multiplayer.entity.living.Player;

import org.json.JSONException;
import org.json.JSONObject;

import static com.codemir.multiplayer.Multiplayer.*;

public class Hud implements Disposable {
    public Stage stage;
    public static Table weaponShopTable;
    public static Table inventoryTable;
    public static Table myBodyTable;
    private Skin skin;
    private Viewport viewport;
    private SpriteBatch batch;
    private String currentWeapon;
    private Label moneyLabel;
    private static Label levelLabel;
    private ImageButton currentWeaponButton;
    private String getShopItemsResponse;
    private String getShopItemAttributesResponse;
    private String getInventoryItemAttributesResponse;
    private String getInventoryItemsResponse;
    private String getEquippedItemsResponse;
    private String addItemToInventoryResponse;
    private String equipItemResponse;
    private List<String> weaponMerchant;
    private List<String> inventory;
    private List<String> equippedItems;
    private Integer itemDamage;
    private Integer itemPrice;
    private Integer itemHealthToAdd;
    private String itemType;

    public Hud(SpriteBatch batch) {
        this.batch = batch;

        skin = new Skin(Gdx.files.internal("pixthulhu/skin/pixthulhu-ui.json"));

        viewport = new FitViewport(V_WIDTH, V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, batch);

        Gdx.input.setInputProcessor(stage);

        moneyLabel = new Label(String.format("%s", Player.money), skin);
        moneyLabel.setPosition(45, 335 - 5);
        moneyLabel.setColor(Color.WHITE);

        levelLabel = new Label(String.format("%s", Player.level), skin);
        levelLabel.setPosition(27, 363);
        levelLabel.setColor(Color.WHITE);
        levelLabel.toFront();

        inventory = new List<String>(skin);
        weaponMerchant = new List<String>(skin);
        equippedItems = new List<String>(skin);

        createInventory();
        createWeaponShop();
        createCurrentWeaponBox();

        stage.addActor(moneyLabel);
        stage.addActor(levelLabel);
    }

    private void createInventory() {
        JSONObject data = new JSONObject();
        try {
            data.put("username", Player.username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getInventoryItems(data, "POST");

        while(getInventoryItemsResponse == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        JSONObject data2 = new JSONObject();
        try {
            data2.put("username", Player.username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getEquippedItems(data2, "POST");

        while(getEquippedItemsResponse == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<String> inv = new List<String>(skin);
        inv.setItems(inventory.getItems());

        List<String> myBody = new List<String>(skin);
        myBody.setItems(equippedItems.getItems());

        inventoryTable = new Table(skin);
        inventoryTable.setFillParent(true);
        inventoryTable.setTransform(true);
        inventoryTable.setOrigin(400, 225);
        inventoryTable.setPosition(-120, 0);
        inventoryTable.setScale(1 / 2f);
        stage.addActor(inventoryTable);
        inventoryTable.setVisible(false);

        inventoryTable.defaults();
        inventoryTable.add("Inventory").row();
        inventoryTable.add(new ScrollPane(inv, skin)).width(400).height(500);

        myBodyTable = new Table(skin);
        myBodyTable.setFillParent(true);
        myBodyTable.setTransform(true);
        myBodyTable.setOrigin(400, 225);
        myBodyTable.setPosition(120, 0);
        myBodyTable.setScale(1 / 2f);
        stage.addActor(myBodyTable);
        myBodyTable.setVisible(false);

        myBodyTable.defaults();
        myBodyTable.add("My Body").row();
        myBodyTable.add(new ScrollPane(myBody, skin)).width(400).height(500);

        DragAndDrop dnd = new DragAndDrop();
        dnd.addSource(new DragAndDrop.Source(inv) {
            final DragAndDrop.Payload payload = new DragAndDrop.Payload();
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                String item = inv.getSelected().substring(0, inv.getSelected().length() - 3);
                payload.setObject(item);
                inv.getItems().removeIndex(inv.getSelectedIndex());
                payload.setDragActor(new Label(item, skin));
                JSONObject data = new JSONObject();
                try {
                    data.put("username", Player.username);
                    data.put("item_name", payload.getObject());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getInventoryItemAttributes(data, "POST");

                while(getInventoryItemAttributesResponse == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return payload;
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                if(target == null)
                    inv.getItems().add((String) payload.getObject());
            }
        });
        dnd.addTarget(new DragAndDrop.Target(myBody) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return itemType.equals("Weapon") || itemType.equals("HeadArmor") || itemType.equals("BodyArmor") || itemType.equals("Shoes");
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                Gdx.app.log("DROP", itemType);
                if (itemType.equals("Weapon")) {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("username", Player.username);
                        data.put("item_type", itemType);
                        data.put("item_name", payload.getObject());
                        data.put("item_damage", itemDamage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    equipItem(data, "POST");

                    while(equipItemResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    JSONObject data2 = new JSONObject();
                    try {
                        data2.put("username", Player.username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getEquippedItems(data2, "POST");

                    while(getEquippedItemsResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    myBody.setItems(equippedItems.getItems());

                    JSONObject data3 = new JSONObject();
                    try {
                        data3.put("username", Player.username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getInventoryItems(data3, "POST");

                    while(getInventoryItemsResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    inv.setItems(inventory.getItems());

                    createCurrentWeaponBox();
                }

                if (itemType.equals("HeadArmor")) {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("username", Player.username);
                        data.put("item_type", itemType);
                        data.put("item_name", payload.getObject());
                        data.put("item_damage", itemDamage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    equipItem(data, "POST");

                    while(equipItemResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    JSONObject data2 = new JSONObject();
                    try {
                        data2.put("username", Player.username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getEquippedItems(data2, "POST");

                    while(getEquippedItemsResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    myBody.setItems(equippedItems.getItems());

                    JSONObject data3 = new JSONObject();
                    try {
                        data3.put("username", Player.username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getInventoryItems(data3, "POST");

                    while(getInventoryItemsResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    inv.setItems(inventory.getItems());
                }

                if (itemType.equals("BodyArmor")) {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("username", Player.username);
                        data.put("item_type", itemType);
                        data.put("item_name", payload.getObject());
                        data.put("item_damage", itemDamage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    equipItem(data, "POST");

                    while(equipItemResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    JSONObject data2 = new JSONObject();
                    try {
                        data2.put("username", Player.username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getEquippedItems(data2, "POST");

                    while(getEquippedItemsResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    myBody.setItems(equippedItems.getItems());

                    JSONObject data3 = new JSONObject();
                    try {
                        data3.put("username", Player.username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getInventoryItems(data3, "POST");

                    while(getInventoryItemsResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    inv.setItems(inventory.getItems());
                }

                if (itemType.equals("Shoes")) {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("username", Player.username);
                        data.put("item_type", itemType);
                        data.put("item_name", payload.getObject());
                        data.put("item_damage", itemDamage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    equipItem(data, "POST");

                    while(equipItemResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    JSONObject data2 = new JSONObject();
                    try {
                        data2.put("username", Player.username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getEquippedItems(data2, "POST");

                    while(getEquippedItemsResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    myBody.setItems(equippedItems.getItems());

                    JSONObject data3 = new JSONObject();
                    try {
                        data3.put("username", Player.username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getInventoryItems(data3, "POST");

                    while(getInventoryItemsResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    inv.setItems(inventory.getItems());
                }
            }
        });
    }

    private void createWeaponShop() {
        JSONObject data = new JSONObject();
        try {
            data.put("username", Player.username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getInventoryItems(data, "POST");

        while(getInventoryItemsResponse == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<String> inv = new List<String>(skin);
        inv.setItems(inventory.getItems());

        JSONObject data2 = new JSONObject();
        try {
            data2.put("item_type", "Weapon");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getShopItems(data2, "POST");

        while(getShopItemsResponse == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        weaponShopTable = new Table(skin);
        weaponShopTable.setFillParent(true);
        weaponShopTable.setTransform(true);
        weaponShopTable.setOrigin(400, 225);
        weaponShopTable.setScale(1 / 2f);
        stage.addActor(weaponShopTable);
        weaponShopTable.setVisible(false);

        weaponShopTable.defaults();
        weaponShopTable.add("Weapon Merchant");
        weaponShopTable.add("Inventory").row();
        weaponShopTable.add(weaponMerchant).expand().fill().width(400).height(500).padRight(34);
        weaponShopTable.add(inv).expand().fill().width(400).height(500).padLeft(34);

        DragAndDrop dnd = new DragAndDrop();
        dnd.addSource(new DragAndDrop.Source(weaponMerchant) {
            final DragAndDrop.Payload payload = new DragAndDrop.Payload();

            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                String item = weaponMerchant.getSelected();
                payload.setObject(item);
                weaponMerchant.getItems().removeIndex(weaponMerchant.getSelectedIndex());
                payload.setDragActor(new Label(item, skin));
                payload.setInvalidDragActor(new Label(item + " (\"You don't have enough money!\")", skin));
                return payload;
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                if (target == null)
                    weaponMerchant.getItems().add((String) payload.getObject());
            }
        });
        dnd.addTarget(new DragAndDrop.Target(inv) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                JSONObject data = new JSONObject();
                try {
                    data.put("item_name", payload.getObject());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getShopItemAttributes(data, "POST");

                while(getShopItemAttributesResponse == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return !(Player.money < itemPrice);
                //return !"Knife".equals(payload.getObject());
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                //inventory.getItems().add((String) payload.getObject());

                JSONObject data = new JSONObject();
                try {
                    data.put("item_name", payload.getObject());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getShopItemAttributes(data, "POST");

                while(getShopItemAttributesResponse == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                JSONObject data2 = new JSONObject();
                try {
                    data2.put("username", Player.username);
                    data2.put("item_type", itemType);
                    data2.put("item_name", payload.getObject());
                    data2.put("item_piece", 1);
                    data2.put("item_damage", itemDamage);
                    data2.put("item_health_to_add", itemHealthToAdd);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                addItemToInventory(data2, "POST");

                JSONObject data3 = new JSONObject();
                try {
                    data3.put("process", "money");
                    data3.put("username", Player.username);
                    data3.put("amount_to_be_reduced", itemPrice);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setPlayerData(data3, "POST", "money");

                while(addItemToInventoryResponse == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                JSONObject data4 = new JSONObject();
                try {
                    data4.put("username", Player.username);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getInventoryItems(data4, "POST");

                while(getInventoryItemsResponse == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                inv.setItems(inventory.getItems());
            }
        });
    }

    private void createCurrentWeaponBox() {
        if (currentWeaponButton != null) {
            currentWeaponButton.remove();
            System.out.println("actor removed!");
        }

        currentWeapon = Player.currentWeapon;

        if (currentWeapon != null && currentWeapon.equals("Iron Sword")) {
            Drawable drawable = new TextureRegionDrawable(new TextureRegion(ironSword, 0, 64, 33, 32));
            currentWeaponButton = new ImageButton(drawable);
            currentWeaponButton.setPosition(760, 410);
        }

        if (currentWeapon != null && currentWeapon.equals("testwep")) {
            System.out.println("diger silah!");
            Drawable drawable = new TextureRegionDrawable(new TextureRegion(ironSword, 33, 64, 33, 32));
            currentWeaponButton = new ImageButton(drawable);
            currentWeaponButton.setPosition(760, 410);
        }

        if (currentWeaponButton != null)
            stage.addActor(currentWeaponButton);
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (!inventoryTable.isVisible() && !myBodyTable.isVisible()) {
                inventoryTable.setVisible(true);
                myBodyTable.setVisible(true);
            } else if (inventoryTable.isVisible() && myBodyTable.isVisible()) {
                inventoryTable.setVisible(false);
                myBodyTable.setVisible(false);
            }
        }
    }

    public void draw(float delta) {
        batch.begin();

        //healthbar
        batch.draw(healthbarBackground, 10, 400);
        float healthbarWidth = (float) Player.health / 100f * 112f;
        healthbar.draw(batch, 49, 406, healthbarWidth, healthbarForeground.getHeight());

        //expbar
        batch.draw(expbarBackground, 10, 365);
        float expbarWidth = (float) Player.exp / (float) Player.nextLevelExp * 112f;
        expbar.draw(batch, 49, 371, expbarWidth, expbarForeground.getHeight());

        //money
        batch.draw(moneyTex, 10, 335);
        moneyLabel.setText(Player.money);

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    public void getShopItems(JSONObject requestObject, String method) {
        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/getshopitems";
        request.setUrl(url);

        request.setContent(requestObject.toString());

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed(shop)");
                    return;
                }

                String responseJson = httpResponse.getResultAsString();
                getShopItemsResponse = responseJson;

                JsonReader json = new JsonReader();
                JsonValue base = json.parse(responseJson);

                Array<String> items = new Array<String>();

                for (int i = 0; i < base.size; i++) {
                    items.add(base.get(i).getString("item_name"));
                }

                weaponMerchant.setItems(items);
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely(shop)");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }

        });
    }

    public void addItemToInventory(JSONObject requestObject, String method) {
        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/additemtoinventory";
        request.setUrl(url);

        request.setContent(requestObject.toString());

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed");
                    return;
                }

                String responseJson = httpResponse.getResultAsString();

                addItemToInventoryResponse = responseJson;

                System.out.println("RESPONSE" + responseJson);
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely inv");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }

        });

    }

    public void getShopItemAttributes(JSONObject requestObject, String method) {

        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/getshopitemattributes";
        request.setUrl(url);

        request.setContent(requestObject.toString());

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed");
                    return;
                }

                String responseJson = httpResponse.getResultAsString();

                getShopItemAttributesResponse = responseJson;

                JsonReader json = new JsonReader();
                JsonValue base = json.parse(responseJson);

                itemPrice = base.getInt("item_price");
                itemDamage = base.getInt("item_damage");
                itemHealthToAdd = base.getInt("item_health_to_add");
                itemType = base.getString("item_type");
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely shop item attr");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }
        });
    }

    public void getInventoryItems(JSONObject requestObject, String method) {
        getInventoryItemsResponse = null;
        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/getinventoryitems";
        request.setUrl(url);

        request.setContent(requestObject.toString());

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed");
                    return;
                }

                String responseJson = httpResponse.getResultAsString();

                getInventoryItemsResponse = responseJson;

                JsonReader json = new JsonReader();
                JsonValue base = json.parse(responseJson);

                Array<String> items = new Array<String>();

                for (int i = 0; i < base.size; i++) {
                    items.add(base.get(i).getString("item_name") + " x" + base.get(i).getInt("item_piece"));
                }

                inventory.setItems(items);
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely get p items");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }
        });
    }

    public void setPlayerData(JSONObject requestObject, String method, String process) {
        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/setplayerdata";
        request.setUrl(url);

        request.setContent(requestObject.toString());

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed");
                    return;
                }

                if (process.equals("money")) {
                    String responseJson = httpResponse.getResultAsString();

                    JsonReader json = new JsonReader();
                    JsonValue base = json.parse(responseJson);

                    Player.money = base.getInt("money");
                }
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely reduce money");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }
        });
    }

    public void getInventoryItemAttributes(JSONObject requestObject, String method) {
        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/getinventoryitemattributes";
        request.setUrl(url);

        request.setContent(requestObject.toString());

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed");
                    return;
                }

                String responseJson = httpResponse.getResultAsString();

                getInventoryItemAttributesResponse = responseJson;

                JsonReader json = new JsonReader();
                JsonValue base = json.parse(responseJson);

                itemType = base.getString("item_type");
                itemDamage = base.getInt("item_damage");
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely get inv attr");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }
        });
    }

    public void equipItem(JSONObject requestObject, String method) {
        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/equipitem";
        request.setUrl(url);

        request.setContent(requestObject.toString());

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed");
                    return;
                }

                String responseJson = httpResponse.getResultAsString();
                equipItemResponse = responseJson;
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely equip");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }
        });
    }

    public void getEquippedItems(JSONObject requestObject, String method) {
        getEquippedItemsResponse = null;
        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/getequippeditems";
        request.setUrl(url);

        request.setContent(requestObject.toString());

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed");
                    return;
                }

                String responseJson = httpResponse.getResultAsString();

                getEquippedItemsResponse = responseJson;

                JsonReader json = new JsonReader();
                JsonValue base = json.parse(responseJson);

                Array<String> items = new Array<String>();

                for (int i = 0; i < base.size; i++) {
                    items.add(base.get(i).getString("item_name"));
                    if (base.get(i).getString("item_type").equals("Weapon")) {
                        Player.currentWeapon = base.get(i).getString("item_name");
                    }
                }

                equippedItems.setItems(items);
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely get equipped");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }
        });
    }

    public static void updateLevel() {
        levelLabel.setText(Player.level);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
