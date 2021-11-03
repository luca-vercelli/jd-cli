/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.visitor;

import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_BOOLEAN;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_BYTE;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_CHAR;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_DOUBLE;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_FLOAT;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_INT;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_LONG;
import static org.jd.core.v1.model.javasyntax.type.PrimitiveType.FLAG_SHORT;

import org.jd.core.v1.model.javasyntax.type.AbstractNopTypeArgumentVisitor;
import org.jd.core.v1.model.javasyntax.type.GenericType;
import org.jd.core.v1.model.javasyntax.type.InnerObjectType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.PrimitiveType;

public class GenerateParameterSuffixNameVisitor extends AbstractNopTypeArgumentVisitor {
    protected String suffix;

    public String getSuffix() {
        return suffix;
    }

    @Override
    public void visit(PrimitiveType type) {
        switch (type.getJavaPrimitiveFlags()) {
        case FLAG_BYTE:
            suffix = "Byte";
            break;
        case FLAG_CHAR:
            suffix = "Char";
            break;
        case FLAG_DOUBLE:
            suffix = "Double";
            break;
        case FLAG_FLOAT:
            suffix = "Float";
            break;
        case FLAG_INT:
            suffix = "Int";
            break;
        case FLAG_LONG:
            suffix = "Long";
            break;
        case FLAG_SHORT:
            suffix = "Short";
            break;
        case FLAG_BOOLEAN:
            suffix = "Boolean";
            break;
        default:
            assert false;
        }
    }

    @Override
    public void visit(ObjectType type) {
        suffix = type.getName();
    }

    @Override
    public void visit(InnerObjectType type) {
        suffix = type.getName();
    }

    @Override
    public void visit(GenericType type) {
        suffix = type.getName();
    }
}
