package shared

import "github.com/jinzhu/gorm"
import _ "github.com/go-sql-driver/mysql"

type DbInfo struct {
	Username string
	Pass     string
	Host     string
	Name     string
}

var Db *gorm.DB
var err error

func init() {
	var dbinf DbInfo
	dbinf.Username = "root"
	dbinf.Pass = "root"
	dbinf.Host = "tcp(127.0.0.1:8889)"
	dbinf.Name = "multiplayer"

	Db, err = gorm.Open("mysql", dbinf.Username+":"+dbinf.Pass+"@"+dbinf.Host+"/"+dbinf.Name+"?charset=utf8&parseTime=True&loc=Local")

	if err != nil {
		panic(err.Error)
	}
}
