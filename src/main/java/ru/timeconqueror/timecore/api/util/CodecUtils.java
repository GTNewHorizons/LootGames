package ru.timeconqueror.timecore.api.util;

import net.minecraft.nbt.NBTTagCompound;
import ru.timeconqueror.lootgames.utils.future.ICodec;

import java.lang.reflect.Array;
import java.util.function.Predicate;

public class CodecUtils {
    @SuppressWarnings("unchecked")
    public static <T> T[][] read2DimArr(NBTTagCompound tableTag, Class<T> elementClass, ICodec<T, NBTTagCompound> elementCodec) {
        int size = tableTag.getInteger("size");

        T[][] table = null;

        for (int i = 0; i < size; i++) {
            NBTTagCompound columnTag = tableTag.getCompoundTag(Integer.toString(i));
            int columnSize = columnTag.getInteger("size");

            T[] column = (T[]) Array.newInstance(elementClass, columnSize);

            for (int j = 0; j < columnSize; j++) {
                if (columnTag.hasKey(Integer.toString(j))) {
                    NBTTagCompound elementTag = columnTag.getCompoundTag(Integer.toString(j));

                    T element = elementCodec.decode(elementTag);
                    column[j] = element;
                }
            }

            if (i == 0) {
                Class<?> columnClass = column.getClass();
                table = (T[][]) Array.newInstance(columnClass, size);
            }

            table[i] = column;
        }

        return table;
    }

    public static <T> NBTTagCompound write2DimArr(T[][] objArr, ICodec<T, NBTTagCompound> elementCodec) {
        return write2DimArr(objArr, elementCodec, e -> true);
    }

    public static <T> NBTTagCompound write2DimArr(T[][] objArr, ICodec<T, NBTTagCompound> elementCodec, Predicate<T> writeElementIf) {
        NBTTagCompound tableTag = new NBTTagCompound();

        for (int i = 0; i < objArr.length; i++) {
            NBTTagCompound column = new NBTTagCompound();

            for (int j = 0; j < objArr[i].length; j++) {
                if (writeElementIf.test(objArr[i][j])) {
                    NBTTagCompound elementTag = elementCodec.encode(objArr[i][j]);
                    column.setTag(Integer.toString(j), elementTag);
                }
            }

            column.setInteger("size", objArr[i].length);

            tableTag.setTag(Integer.toString(i), column);
        }

        tableTag.setInteger("size", objArr.length);

        return tableTag;
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException() {
        }

        public NotFoundException(String message) {
            super(message);
        }

        public NotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public NotFoundException(Throwable cause) {
            super(cause);
        }

        public NotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
