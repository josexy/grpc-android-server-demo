package handler

import (
	"io"
	pb "simplerpcclient/protobuf"

	"google.golang.org/grpc"
)

type WriteCallback func([]byte) error

func (c WriteCallback) Write(bytes []byte) error {
	return c(bytes)
}

type ReadCallback func() ([]byte, error)

func (c ReadCallback) Read() ([]byte, error) {
	return c()
}

type Handler struct {
	cs grpc.ClientStream
}

func NewStreamHandler(cs grpc.ClientStream) *Handler {
	return &Handler{cs: cs}
}

func (h *Handler) HandleGet(callback WriteCallback) error {
	m := new(pb.Bytes)
	var err error
	for {
		err = h.cs.RecvMsg(m)
		if err != nil {
			if err == io.EOF {
				err = nil
			}
			break
		}
		err = callback.Write(m.Value)
		if err != nil {
			break
		}
	}
	err = h.CloseSend()
	return err
}

func (h *Handler) HandlePut(callback ReadCallback) error {
	var err error
	m := new(pb.Bytes)
	for {
		m.Value, err = callback.Read()
		if err != nil {
			if err == io.EOF {
				err = nil
			}
			break
		}
		err = h.cs.SendMsg(m)
		if err != nil {
			break
		}
	}
	err = h.CloseSend()
	return err
}

func (h *Handler) CloseSend() error {
	return h.cs.CloseSend()
}

func (h *Handler) GetFinalResult() (*pb.RespFileInfo, error) {
	m := new(pb.RespFileInfo)
	if err := h.cs.RecvMsg(m); err != nil {
		return nil, err
	}
	return m, nil
}
