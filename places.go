// Quick and dirty script to get stop ids.
// See usage below in main()
package main

import (
	"archive/zip"
	"encoding/binary"
	"fmt"
	"io"
	"io/ioutil"
	"os"
	"strings"
)

func main() {
	if len(os.Args) < 3 {
		fmt.Println("Usage: go run places.go PATH_TO_PUBTRAN_APK PART_OF_STOP_NAME [CITY]")
		fmt.Println("    go run places.go pubtran.apk vojtova")
		fmt.Println("    go run places.go pubtran.apk \"hlavni nadrazi\" brno")
		return
	}

	fn := os.Args[1]
	expr := os.Args[2]
	exprLower := strings.ToLower(expr)
	var city string
	if len(os.Args) >= 4 {
		city = strings.ToLower(os.Args[3])
	}

	var f io.ReadCloser
	if strings.HasSuffix(fn, ".apk") {
		zf, _ := zip.OpenReader(fn)
		defer zf.Close()

		for _, e := range zf.File {
			if e.Name == "assets/places.bin" {
				f, _ = e.Open()
				defer f.Close()
				break
			}
		}
	} else {
		f, _ = os.Open(os.Args[1])
		defer f.Close()
	}

	cities := readSimpleBlock(f, "city")
	districts := readSimpleBlock(f, "district")

	for i := 0; true; i++ {
		var id uint32
		if err := binary.Read(f, binary.BigEndian, &id); err != nil {
			return
		}

		io.CopyN(ioutil.Discard, f, 2) // flags?

		var cityId uint16
		binary.Read(f, binary.BigEndian, &cityId)

		var districtId uint8
		binary.Read(f, binary.BigEndian, &districtId)

		io.CopyN(ioutil.Discard, f, 1) // ???

		var coords [2]int32
		binary.Read(f, binary.BigEndian, &coords)
		x := float64(coords[0]) / 1000000
		y := float64(coords[1]) / 1000000

		searchName := readSizedStr(f)
		fullName := readSizedStr(f)

		if (strings.Contains(searchName, exprLower) || strings.Contains(fullName, expr)) &&
			(city == "" || strings.Contains(strings.ToLower(cities[uint32(cityId)]), city)) {
			fmt.Printf("#%d 0x%08x, %f, %f %s (%s) - %s, %s\n", i, id, x, y, fullName, searchName, cities[uint32(cityId)], districts[uint32(districtId)])
		}
	}
}

func readSizedStr(f io.ReadCloser) string {
	var strlen uint8
	binary.Read(f, binary.BigEndian, &strlen)

	name := make([]byte, strlen)
	io.ReadFull(f, name)
	return string(name)
}

func readSimpleBlock(f io.ReadCloser, blockname string) map[uint32]string {
	res := map[uint32]string{}
	var cnt uint32
	binary.Read(f, binary.BigEndian, &cnt)
	for i := uint32(0); i < cnt; i++ {
		var id uint32
		binary.Read(f, binary.BigEndian, &id)

		name := readSizedStr(f)
		//fmt.Printf("%s %d %08x %s\n", blockname, i, id, name)
		res[id] = name
	}

	//offset, _ := f.Seek(0, io.SeekCurrent)
	//fmt.Printf("%s end offset %d %x\n", blockname, offset, offset)
	return res
}
