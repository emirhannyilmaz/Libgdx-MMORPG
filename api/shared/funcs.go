package shared

import (
	"crypto/md5"
	"encoding/hex"
)

func Encrypt(s string) string {
	md5HashInBytes := md5.Sum([]byte(s))
	md5HashInString := hex.EncodeToString(md5HashInBytes[:])

	return md5HashInString
}
