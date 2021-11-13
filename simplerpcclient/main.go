package main

import (
	"bufio"
	"context"
	"log"
	"os"
	"simplerpcclient/handler"
	pb "simplerpcclient/protobuf"
	"time"

	"google.golang.org/grpc"
)

var conn *grpc.ClientConn

func ConnectRpcServer() {
	ctx, cancel := context.WithTimeout(context.Background(), 4*time.Second)
	defer cancel()
	var err error
	conn, err = grpc.DialContext(ctx, "127.0.0.1:6666", grpc.WithInsecure(), grpc.WithNoProxy(), grpc.WithBlock())
	if err != nil {
		log.Fatalln(err)
		return
	}
}

func CloseRpcServer() {
	if conn != nil {
		_ = conn.Close()
	}
}

type Context struct {
	ctx    context.Context
	client pb.FileServiceClient
}

func NewContext() *Context {
	return &Context{
		ctx:    context.Background(),
		client: pb.NewFileServiceClient(conn),
	}
}

func (c *Context) UploadFile(filename string) {
	s, err := c.client.UploadFile(c.ctx)
	if err != nil {
		log.Println(err)
		return
	}
	h := handler.NewStreamHandler(s)

	f, err := os.Open(filename)
	if err != nil {
		log.Println(err)
		return
	}
	defer func() { _ = f.Close() }()

	buf := bufio.NewReader(f)
	tmp := make([]byte, 0, 4096)
	_ = h.HandlePut(func() ([]byte, error) {
		var n int
		n, err = buf.Read(tmp[:4096])
		if n > 0 {
			log.Println("send", n, "bytes")
			return tmp[:n], err
		}
		return nil, err
	})
	rs, err := h.GetFinalResult()
	log.Println(rs)
}

func (c *Context) DownloadFile(filepath, save string) {
	s, err := c.client.DownloadFile(c.ctx, &pb.ReqFilePath{Value: filepath})
	if err != nil {
		log.Println(err)
		return
	}
	h := handler.NewStreamHandler(s)

	f, err := os.Create(save)
	if err != nil {
		log.Println(err)
		return
	}
	defer func() { _ = f.Close() }()

	buf := bufio.NewWriter(f)
	_ = h.HandleGet(func(bytes []byte) error {
		log.Println("receive", len(bytes), "bytes")
		_, err = buf.Write(bytes)
		return err
	})
	_ = buf.Flush()
}

func (c *Context) SayHello(message string) string {
	var resp *pb.RespHello
	resp, err := c.client.SayHello(c.ctx, &pb.ReqHello{Value: message})
	if err != nil {
		return ""
	}
	return resp.Value
}

func main() {
	ConnectRpcServer()
	defer CloseRpcServer()
	ctx := NewContext()

	reply := ctx.SayHello("hello world")
	log.Println(reply)

	ctx.UploadFile("./go.sum")

	ctx.DownloadFile("/storage/emulated/0/Download/upload_file", "save_file")
}
