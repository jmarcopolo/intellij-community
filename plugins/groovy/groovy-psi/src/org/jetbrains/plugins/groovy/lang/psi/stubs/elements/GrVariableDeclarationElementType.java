/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.lang.psi.stubs.elements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinitionBody;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.GrVariableDeclarationImpl;
import org.jetbrains.plugins.groovy.lang.psi.stubs.GrStubUtils;
import org.jetbrains.plugins.groovy.lang.psi.stubs.GrVariableDeclarationStub;

import java.io.IOException;

public class GrVariableDeclarationElementType extends GrStubElementType<GrVariableDeclarationStub, GrVariableDeclaration> {

  public GrVariableDeclarationElementType() {
    super("variable definitions");
  }

  @Override
  public void serialize(@NotNull GrVariableDeclarationStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    GrStubUtils.writeNullableString(dataStream, stub.getTypeString());
  }

  @NotNull
  @Override
  public GrVariableDeclarationStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GrVariableDeclarationStub(
      parentStub,
      GrStubUtils.readNullableString(dataStream)
    );
  }

  @Override
  public GrVariableDeclaration createPsi(@NotNull GrVariableDeclarationStub stub) {
    return new GrVariableDeclarationImpl(stub);
  }

  @NotNull
  @Override
  public GrVariableDeclarationStub createStub(@NotNull GrVariableDeclaration psi, StubElement parentStub) {
    return new GrVariableDeclarationStub(parentStub, GrStubUtils.getTypeText(psi.getTypeElementGroovy()));
  }

  @Override
  public boolean shouldCreateStub(ASTNode node) {
    PsiElement parent = SharedImplUtil.getParent(node);
    if (parent instanceof GrTypeDefinitionBody) {
      // store fields
      return true;
    }
    if (PsiTreeUtil.getParentOfType(parent, GrTypeDefinition.class) != null) {
      // do not store variable declarations within classes, as they are not scripts
      return false;
    }
    PsiElement psi = node.getPsi();
    if (!(psi instanceof GrVariableDeclaration) || ((GrVariableDeclaration)psi).getModifierList().getRawAnnotations().length == 0) {
      // store only annotated declarations
      return false;
    }
    PsiFile file = psi.getContainingFile();
    return file instanceof GroovyFile && ((GroovyFile)file).isScript();
  }
}
