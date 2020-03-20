package main

import (
	"encoding/json"
	"fmt"

	"api/shared"

	"github.com/gin-gonic/gin"
)

type Player struct {
	ID           string
	Email        string `json:"email"`
	Username     string `json:"username"`
	Password     string `json:"password"`
	Health       int    `json:"health"`
	Money        int    `json:"money"`
	Level        int    `json:"level"`
	Exp          int    `json:"exp"`
	NextLevelExp int    `json:"next_level_exp"`
}

type Inventory struct {
	ID              string
	Username        string `json:"username"`
	ItemType        string `json:"item_type"`
	ItemName        string `json:"item_name"`
	ItemPiece       int    `json:"item_piece"`
	ItemDamage      int    `json:"item_damage"`
	ItemHealthToAdd int    `json:"item_health_to_add"`
}

type Shop_Items struct {
	ItemType        string `json:"item_type"`
	ItemName        string `json:"item_name"`
	ItemPrice       int    `json:"item_price"`
	ItemDamage      int    `json:"item_damage"`
	ItemHealthToAdd int    `json:"item_health_to_add"`
}

type EquippedItems struct {
	ID         string
	Username   string `json:"username"`
	ItemType   string `json:"item_type"`
	ItemName   string `json:"item_name"`
	ItemDamage int    `json:"item_damage"`
}

var Email string
var Username string
var Password string
var Health int
var Money int

func LoginControl(c *gin.Context) {
	var player Player
	c.BindJSON(&player)

	Username = player.Username
	Password = player.Password

	player.Email = ""
	player.Username = ""
	player.Password = ""

	shared.Db.Where("username=? and password=?", Username, shared.Encrypt(Password)).First(&player)
	if player.Username != "" {
		greetings := struct{ RegisteredPlayer string }{"true"}
		returnedJSON, err := json.Marshal(greetings)

		if err != nil {
			panic(err.Error())
		}

		c.Writer.Header().Set("Content-Type", "application/json")
		c.Writer.Write(returnedJSON)
	} else {
		greetings := struct{ RegisteredPlayer string }{"false"}
		returnedJSON, err := json.Marshal(greetings)

		if err != nil {
			panic(err.Error())
		}

		c.Writer.Header().Set("Content-Type", "application/json")
		c.Writer.Write(returnedJSON)
	}

	Email = ""
	Username = ""
	Password = ""
}

func Register(c *gin.Context) {
	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Header().Set("Access-Control-Allow-Origin", "*")

	var player Player
	c.BindJSON(&player)

	player.Password = shared.Encrypt(player.Password)
	player.Health = 100
	player.Money = 50
	player.Level = 1
	player.Exp = 0
	player.NextLevelExp = 100

	shared.Db.Create(&player)

	fmt.Println("Player registered to database!")
	fmt.Println(&player)

	player.Email = ""
	player.Username = ""
	player.Password = ""
	player.Money = 0
	player.Health = 0
	player.Level = 0
	player.Exp = 0
	player.NextLevelExp = 0
}

func EmailControl(c *gin.Context) {
	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Header().Set("Access-Control-Allow-Origin", "*")

	var player Player
	c.BindJSON(&player)

	Email = player.Email

	player.Email = ""
	player.Username = ""
	player.Password = ""

	shared.Db.Where("email=?", Email).First(&player)
	if player.Email != "" {
		greetings := struct{ EmailAvailable string }{"false"}
		returnedJSON, err := json.Marshal(greetings)

		if err != nil {
			panic(err.Error())
		}

		c.Writer.Header().Set("Content-Type", "application/json")
		c.Writer.Write(returnedJSON)
	} else {
		greetings := struct{ EmailAvailable string }{"true"}
		returnedJSON, err := json.Marshal(greetings)

		if err != nil {
			panic(err.Error())
		}

		c.Writer.Header().Set("Content-Type", "application/json")
		c.Writer.Write(returnedJSON)
	}

	Email = ""
	Username = ""
	Password = ""
}

func UsernameControl(c *gin.Context) {
	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Header().Set("Access-Control-Allow-Origin", "*")

	var player Player
	c.BindJSON(&player)

	Username = player.Username

	player.Email = ""
	player.Username = ""
	player.Password = ""

	shared.Db.Where("username=?", Username).First(&player)
	if player.Username != "" {
		greetings := struct{ UsernameAvailable string }{"false"}
		returnedJSON, err := json.Marshal(greetings)

		if err != nil {
			panic(err.Error())
		}

		c.Writer.Header().Set("Content-Type", "application/json")
		c.Writer.Write(returnedJSON)
	} else {
		greetings := struct{ UsernameAvailable string }{"true"}
		returnedJSON, err := json.Marshal(greetings)

		if err != nil {
			panic(err.Error())
		}

		c.Writer.Header().Set("Content-Type", "application/json")
		c.Writer.Write(returnedJSON)
	}

	Email = ""
	Username = ""
	Password = ""
}

func GetPlayerData(c *gin.Context) {
	var equippedItems EquippedItems

	var data struct {
		Username            string `json:"username"`
		Health              string `json:"health"`
		Money               string `json:"money"`
		Level               int    `json:"level"`
		Exp                 int    `json:"exp"`
		NextLevelExp        int    `json:"next_level_exp"`
		CurrentWeapon       string `json:"current_weapon"`
		CurrentWeaponDamage int    `json:"current_weapon_damage"`
	}

	c.BindJSON(&data)

	shared.Db.Table("players").Where("username=?", data.Username).First(&data)
	shared.Db.Where("username=? AND item_type=?", data.Username, "Weapon").First(&equippedItems)

	data.CurrentWeapon = equippedItems.ItemName
	data.CurrentWeaponDamage = equippedItems.ItemDamage

	greetings := &data
	returnedJSON, err := json.Marshal(greetings)

	if err != nil {
		panic(err.Error())
	}

	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Write(returnedJSON)

	Email = ""
	Username = ""
	Password = ""
	Health = 0
	Money = 0
}

func AddItemToInventory(c *gin.Context) {
	var inventory Inventory
	c.BindJSON(&inventory)

	shared.Db.Create(&inventory)

	greetings := "added"
	returnedJSON, err := json.Marshal(greetings)

	if err != nil {
		panic(err.Error())
	}

	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Write(returnedJSON)
}

func GetShopItems(c *gin.Context) {
	var items Shop_Items

	c.BindJSON(&items)

	var i []Shop_Items

	shared.Db.Where("item_type=?", items.ItemType).Find(&i)

	greetings := &i
	returnedJSON, err := json.Marshal(greetings)

	if err != nil {
		panic(err.Error())
	}

	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Write(returnedJSON)
}

func GetShopItemAttributes(c *gin.Context) {
	var items Shop_Items
	c.BindJSON(&items)

	shared.Db.Where("item_name=?", items.ItemName).First(&items)

	greetings := &items
	returnedJSON, err := json.Marshal(greetings)

	if err != nil {
		panic(err.Error())
	}

	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Write(returnedJSON)
}

func GetInventoryItems(c *gin.Context) {
	var inventory Inventory

	c.BindJSON(&inventory)

	var i []Inventory

	shared.Db.Where("username=?", inventory.Username).Find(&i)

	greetings := &i
	returnedJSON, err := json.Marshal(greetings)

	if err != nil {
		panic(err.Error())
	}

	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Write(returnedJSON)
}

func SetPlayerData(c *gin.Context) {
	var player Player
	var updatedMoney int

	var data struct {
		Process           string `json:"process"`
		Username          string `json:"username"`
		AmountToBeReduced int    `json:"amount_to_be_reduced"`
		Health            int    `json:"health"`
		Level             int    `json:"level"`
		Exp               int    `json:"exp"`
		NextLevelExp      int    `json:"next_level_exp"`
	}

	c.BindJSON(&data)

	if data.Process == "money" {
		shared.Db.Where("username=?", data.Username).First(&player)
		updatedMoney = player.Money - data.AmountToBeReduced

		shared.Db.Model(&player).Where("username=?", data.Username).Update("money", updatedMoney)
		shared.Db.Where("username=?", data.Username).First(&player)

		greetings := &player
		returnedJSON, err := json.Marshal(greetings)

		if err != nil {
			panic(err.Error())
		}

		c.Writer.Header().Set("Content-Type", "application/json")
		c.Writer.Write(returnedJSON)
	}

	if data.Process == "health" {
		shared.Db.Model(&player).Where("username=?", data.Username).Update("health", data.Health)
	}

	if data.Process == "level" {
		shared.Db.Model(&player).Where("username=?", data.Username).Update("level", data.Level)
	}

	if data.Process == "exp" {
		shared.Db.Model(&player).Where("username=?", data.Username).Update("exp", data.Exp)
	}

	if data.Process == "next_level_exp" {
		shared.Db.Model(&player).Where("username=?", data.Username).Update("next_level_exp", data.NextLevelExp)
	}
}

func GetInventoryItemAttributes(c *gin.Context) {
	var item Inventory
	c.BindJSON(&item)

	shared.Db.Where("username=? AND item_name=?", item.Username, item.ItemName).First(&item)

	greetings := &item
	returnedJSON, err := json.Marshal(greetings)

	if err != nil {
		panic(err.Error())
	}

	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Write(returnedJSON)
}

func EquipItem(c *gin.Context) {
	var equippedItems EquippedItems
	var ei EquippedItems
	var previousItem EquippedItems
	//var receivedItemType string

	c.BindJSON(&equippedItems)

	ei.ID = ""
	ei.Username = ""
	ei.ItemType = ""
	ei.ItemName = ""

	shared.Db.Where("username=? AND item_type=?", equippedItems.Username, equippedItems.ItemType).First(&ei)

	if ei.ItemType != "" {
		shared.Db.Where("username=? AND item_type=?", equippedItems.Username, equippedItems.ItemType).First(&previousItem)
		shared.Db.Where("username=? AND item_type=?", equippedItems.Username, equippedItems.ItemType).Delete(&EquippedItems{})
		shared.Db.Create(&equippedItems)
		shared.Db.Where("username=? AND item_name=?", equippedItems.Username, equippedItems.ItemName).Delete(&Inventory{})
		item := Inventory{Username: previousItem.Username, ItemType: previousItem.ItemType, ItemName: previousItem.ItemName, ItemPiece: 1, ItemDamage: previousItem.ItemDamage, ItemHealthToAdd: 0}
		shared.Db.Create(&item).Model(&item)
	} else {
		shared.Db.Create(&equippedItems)
		shared.Db.Where("username=? AND item_name=?", equippedItems.Username, equippedItems.ItemName).Delete(&Inventory{})
	}

	greetings := "equipped"
	returnedJSON, err := json.Marshal(greetings)

	if err != nil {
		panic(err.Error())
	}

	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Write(returnedJSON)
}

func GetEquippedItems(c *gin.Context) {
	var equippedItems EquippedItems
	var i []EquippedItems

	c.BindJSON(&equippedItems)

	shared.Db.Where("username=?", equippedItems.Username).Find(&i)

	greetings := &i
	returnedJSON, err := json.Marshal(greetings)

	if err != nil {
		panic(err.Error())
	}

	c.Writer.Header().Set("Content-Type", "application/json")
	c.Writer.Write(returnedJSON)
}

func main() {
	r := gin.Default()

	r.POST("/register", Register)
	r.POST("/login", LoginControl)
	r.POST("/email", EmailControl)
	r.POST("/username", UsernameControl)
	r.POST("/getplayerdata", GetPlayerData)
	r.POST("/additemtoinventory", AddItemToInventory)
	r.POST("/getshopitems", GetShopItems)
	r.POST("/getshopitemattributes", GetShopItemAttributes)
	r.POST("/getinventoryitems", GetInventoryItems)
	r.POST("/setplayerdata", SetPlayerData)
	r.POST("/getinventoryitemattributes", GetInventoryItemAttributes)
	r.POST("/equipitem", EquipItem)
	r.POST("/getequippeditems", GetEquippedItems)

	r.Run(":8081")
}
