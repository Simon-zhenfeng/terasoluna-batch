/*
 * Copyright (c) 2007 NTT DATA Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.terasoluna.fw.file.dao.standard;

import jp.terasoluna.fw.file.dao.FileLineIterator;

/**
 * 可変長ファイル読取用のFileLineIterator生成クラス。
 * <p>
 * 可変長ファイルからファイル行オブジェクトを生成する<code>FileLineIterator</code> を生成するためのクラスである。引数にはデータを読み取る可変長ファイルのパスと ファイル行オブジェクトクラスを設定すること。
 * </p>
 * 行オブジェクトに設定出来るアノテーションの説明は{@link VariableFileLineIterator} のJavaDocを参考して下さい。
 */
public class VariableFileQueryDAO extends AbstractFileQueryDAO {

    /**
     * FileLineIterator取得用メソッド。
     * @param <T> 1行分の文字列を格納するファイル行オブジェクトクラス
     * @param fileName ファイル名
     * @param clazz パラメータクラス
     * @return 可変長ファイル読取用オブジェクト
     */
    @Override
    public <T> FileLineIterator<T> execute(String fileName, Class<T> clazz) {

        // FileLineIteratorを生成する。
        VariableFileLineIterator<T> fileLineIterator = new VariableFileLineIterator<T>(
                fileName, clazz, getColumnParserMap());

        return fileLineIterator;
    }
}
