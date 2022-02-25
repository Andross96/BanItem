/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2021 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your action) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Package utils that use the reflection api of java
 * @version 3.4
 * @author EpiCanard
 */
public class ReflectionUtils {

    private final static String bukkitPackageVersion;

    static {
        // Bukkit package version (ex: V1_8_R3)
        bukkitPackageVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    /**
     * Create an NMSItemStack from Bukkit ItemStack
     * @param itemStack Bukkit ItemStack to convert
     * @return Converted ItemStack
     */
    public static Object asNMSCopy(@NotNull final ItemStack itemStack) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final String craftPath = String.format("org.bukkit.craftbukkit.%s.inventory.CraftItemStack", bukkitPackageVersion);
        final Class<?> craftItemStack =  Class.forName(craftPath);
        final Method asNMSCopy = craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
        return asNMSCopy.invoke(null, itemStack);
    }

    /**
     * Call the first method matching the returnType
     * @param obj Object that contains the method
     * @param returnType Return type class that must be returned by the method
     * @param <T> Return type
     * @return The value returned by the call to the method
     */
    @SuppressWarnings("unchecked")
    public static <T> T callMethodWithReturnType(@NotNull final Object obj, @NotNull final Class<? extends T> returnType) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Optional<Method> maybeMethod = Arrays.stream(obj.getClass().getDeclaredMethods()).filter(m -> m.getReturnType() == returnType).findFirst();
        if (maybeMethod.isPresent())
            return (T)maybeMethod.get().invoke(obj);
        throw new NoSuchMethodException("Can't find method with type : " + returnType.getName());
    }

    /**
     * Call the first method matching the name
     * @param obj Object that contains the method
     * @param name Name of method to call
     * @param <T> Return type
     * @return The value returned by the call to the method
     */
    @SuppressWarnings("unchecked")
    public static <T> T callMethodWithName(@NotNull final Object obj, @NotNull final String name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (T)obj.getClass().getDeclaredMethod(name).invoke(obj);
    }
}
