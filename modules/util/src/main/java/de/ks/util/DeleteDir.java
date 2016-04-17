/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DeleteDir {
  private final Path dir;
  boolean deleteFolder = true;

  public DeleteDir(Path dir) {
    this.dir = dir;
  }

  public DeleteDir(File dir) {
    this.dir = dir.toPath();
  }

  public boolean isDeleteFolder() {
    return deleteFolder;
  }

  public DeleteDir setDeleteFolder(boolean deleteFolder) {
    this.deleteFolder = deleteFolder;
    return this;
  }

  public void delete() {
    try {
      deleteDir(dir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void deleteDir(Path root) throws IOException {
    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (deleteFolder || !root.equals(dir)) {
          Files.delete(dir);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
