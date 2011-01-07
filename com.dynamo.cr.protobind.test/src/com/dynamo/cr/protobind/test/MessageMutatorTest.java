package com.dynamo.cr.protobind.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import com.dynamo.cr.protobind.IFieldPath;
import com.dynamo.cr.protobind.IMessageDecriptor;
import com.dynamo.cr.protobind.MessageMutator;
import com.dynamo.cr.protobind.internal.FieldPath;
import com.dynamo.cr.protobind.proto.ProtoBind.NestedMessage;
import com.dynamo.cr.protobind.proto.ProtoBind.NestedNestedMessage;
import com.dynamo.cr.protobind.proto.ProtoBind.SimpleMessage;

public class MessageMutatorTest {

    public MessageMutatorTest() {
    }

    @Test
    public void testFields() {
        SimpleMessage.Builder builder = SimpleMessage.newBuilder();
        builder.setA(1);
        builder.setB(123);
        builder.setS("foo");
        SimpleMessage simpleMessage = builder.build();

        MessageMutator<SimpleMessage> mutable = new MessageMutator<SimpleMessage>(simpleMessage);
        IMessageDecriptor desc = mutable.getMessageDescriptor();
        FieldPath[] fieldPaths = desc.getFieldPaths();
        HashMap<String, FieldPath> fieldPathsMap = new HashMap<String, FieldPath>();
        for (FieldPath d : fieldPaths) {
            fieldPathsMap.put(d.getName(), d);
        }

        assertEquals(3, fieldPaths.length);
        assertTrue(fieldPathsMap.containsKey("a"));
        assertTrue(fieldPathsMap.containsKey("b"));
        assertTrue(fieldPathsMap.containsKey("s"));
    }

    @Test
    public void testFieldByName() {
        SimpleMessage.Builder builder = SimpleMessage.newBuilder();
        builder.setA(1);
        builder.setB(123);
        builder.setS("foo");
        SimpleMessage simpleMessage = builder.build();

        MessageMutator<SimpleMessage> mutable = new MessageMutator<SimpleMessage>(simpleMessage);
        IMessageDecriptor desc = mutable.getMessageDescriptor();
        assertEquals("a", desc.findFieldByName("a").getName());
        assertEquals("b", desc.findFieldByName("b").getName());
        assertEquals("s", desc.findFieldByName("s").getName());
    }

    @Test
    public void testSetGetSimple() {
        SimpleMessage.Builder builder = SimpleMessage.newBuilder();
        builder.setA(1);
        builder.setB(123);
        builder.setS("foo");
        SimpleMessage simpleMessage = builder.build();

        MessageMutator<SimpleMessage> mutable = new MessageMutator<SimpleMessage>(simpleMessage);
        IMessageDecriptor desc = mutable.getMessageDescriptor();

        assertEquals(1, mutable.getField(desc.findFieldByName("a")));
        assertEquals(123.0f, mutable.getField(desc.findFieldByName("b")));
        assertEquals("foo", mutable.getField(desc.findFieldByName("s")));

        mutable.setField(desc.findFieldByName("a"), 10);
        mutable.setField(desc.findFieldByName("b"), 1234.0f);
        mutable.setField(desc.findFieldByName("s"), "foobar");

        assertEquals(10, mutable.getField(desc.findFieldByName("a")));
        assertEquals(1234.0f, mutable.getField(desc.findFieldByName("b")));
        assertEquals("foobar", mutable.getField(desc.findFieldByName("s")));
    }

    @Test
    public void testNested() {
        NestedMessage.Builder builder = NestedMessage.newBuilder();
        builder.setMsg(SimpleMessage.newBuilder().setA(1).setB(123).setS("foo")).setX(88);

        NestedMessage nestedMessage = builder.build();

        MessageMutator<NestedMessage> mutable = new MessageMutator<NestedMessage>(nestedMessage);
        IMessageDecriptor desc = mutable.getMessageDescriptor();
        assertEquals("NestedMessage", desc.getName());

        IFieldPath msgFieldPaths = desc.findFieldByName("msg");
        IMessageDecriptor msgDesc = msgFieldPaths.getMessageDescriptor();
        assertEquals("SimpleMessage", msgDesc.getName());
        assertEquals("NestedMessage", msgFieldPaths.getContainingDescriptor().getName());

        assertEquals(88, mutable.getField(desc.findFieldByName("x")));
        assertEquals(1, mutable.getField(msgDesc.findFieldByName("a")));
        assertEquals(123.0f, mutable.getField(msgDesc.findFieldByName("b")));
        assertEquals("foo", mutable.getField(msgDesc.findFieldByName("s")));

        mutable.setField(desc.findFieldByName("x"), 99);
        mutable.setField(msgDesc.findFieldByName("a"), 2);
        mutable.setField(msgDesc.findFieldByName("b"), 456.0f);
        mutable.setField(msgDesc.findFieldByName("s"), "hello");

        assertEquals(99, mutable.getField(desc.findFieldByName("x")));
        assertEquals(2, mutable.getField(msgDesc.findFieldByName("a")));
        assertEquals(456.0f, mutable.getField(msgDesc.findFieldByName("b")));
        assertEquals("hello", mutable.getField(msgDesc.findFieldByName("s")));

        NestedMessage nestedMessage2 = mutable.build();
        assertEquals(99, nestedMessage2.getX());
        assertEquals(2, nestedMessage2.getMsg().getA());
        assertEquals(456.0f, nestedMessage2.getMsg().getB(), 0.001f);
        assertEquals("hello", nestedMessage2.getMsg().getS());
    }

    @Test
    public void testNestedNested() {
        SimpleMessage.Builder simple = SimpleMessage.newBuilder().setA(1).setB(123).setS("foo");
        NestedMessage.Builder nested = NestedMessage.newBuilder().setX(88).setMsg(simple);

        NestedNestedMessage.Builder builder = NestedNestedMessage.newBuilder();
        builder.setNested(nested);
        NestedNestedMessage nestedNestedMessage = builder.build();

        MessageMutator<NestedNestedMessage> mutable = new MessageMutator<NestedNestedMessage>(nestedNestedMessage);
        IMessageDecriptor desc = mutable.getMessageDescriptor();
        assertEquals("NestedNestedMessage", desc.getName());

        IFieldPath nestedFieldPaths = desc.findFieldByName("nested");
        IMessageDecriptor nestedDesc = nestedFieldPaths.getMessageDescriptor();
        assertEquals("NestedMessage", nestedDesc.getName());
        assertEquals("NestedNestedMessage", nestedFieldPaths.getContainingDescriptor().getName());

        IFieldPath msgFieldPaths = nestedDesc.findFieldByName("msg");
        IMessageDecriptor msgDesc = msgFieldPaths.getMessageDescriptor();
        assertEquals("SimpleMessage", msgDesc.getName());
        assertEquals("NestedMessage", msgFieldPaths.getContainingDescriptor().getName());

        assertEquals(88, mutable.getField(nestedDesc.findFieldByName("x")));
        assertEquals(1, mutable.getField(msgDesc.findFieldByName("a")));
        assertEquals(123.0f, mutable.getField(msgDesc.findFieldByName("b")));
        assertEquals("foo", mutable.getField(msgDesc.findFieldByName("s")));

        mutable.setField(nestedDesc.findFieldByName("x"), 99);
        mutable.setField(msgDesc.findFieldByName("a"), 2);
        mutable.setField(msgDesc.findFieldByName("b"), 456.0f);
        mutable.setField(msgDesc.findFieldByName("s"), "hello");

        assertEquals(99, mutable.getField(nestedDesc.findFieldByName("x")));
        assertEquals(2, mutable.getField(msgDesc.findFieldByName("a")));
        assertEquals(456.0f, mutable.getField(msgDesc.findFieldByName("b")));
        assertEquals("hello", mutable.getField(msgDesc.findFieldByName("s")));

        NestedNestedMessage nestedNestedMessage2 = mutable.build();
        assertEquals(99, nestedNestedMessage2.getNested().getX());
        assertEquals(2, nestedNestedMessage2.getNested().getMsg().getA());
        assertEquals(456.0f, nestedNestedMessage2.getNested().getMsg().getB(), 0.001f);
        assertEquals("hello", nestedNestedMessage2.getNested().getMsg().getS());
    }

}
