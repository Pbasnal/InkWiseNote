namespace UtilsLibrary;

public class ArrayUtils
{
    // since the array could be a preallocated array, valueTypeArray.Length might not be
    // equal to the actual amount of values in the array.
    public static int FindInsertLocationNoDuplicates<T>(T value,
        T[] valueTypeArray,
        int countOfExistingElementsInArray,
        Func<T, T, int> comparator)
    {
        if (countOfExistingElementsInArray >= valueTypeArray.Length)
        {
            return -99;
        }

        int insertLocation = 0;
        for (; insertLocation < countOfExistingElementsInArray; insertLocation++)
        {
            // in case the element already exists, then caller will have to run a separate search to find it's index
            if (comparator(valueTypeArray[insertLocation], value) == 0) return -1;
            if (comparator(valueTypeArray[insertLocation], value) < 0) continue;

            break;
        }

        return insertLocation;
    }


    public static bool ShiftRight<T>(T[] array, int fromIndex, int countOfExistingElementsInArray, Action<int, int> copyAction)
    {
        if (countOfExistingElementsInArray >= array.Length)
        {
            return false;
        }

        // countOfExistingElementsInArray is like length. So it'll be lastIndex + 1
        for (int i = countOfExistingElementsInArray - 1; i >= fromIndex; i--)
        {
            copyAction(i, i + 1);
        }

        return true;
    }

    public static bool ShiftLeft<T>(T[] array, int fromIndex, int countOfExistingElementsInArray, Action<int, int> copyAction)
    {
        if (countOfExistingElementsInArray >= array.Length)
        {
            return false;
        }

        // countOfExistingElementsInArray is like length. So it'll be lastIndex + 1
        for (int i = fromIndex; i < countOfExistingElementsInArray; i++)
        {
            copyAction(i + 1, i);
        }

        return true;
    }

    public static int FindIndexOf<T>(T value, T[] array, Func<T, T, int> comparer, int startIndex, int endIndex)
    {
        int mid = (startIndex + endIndex) / 2 + 1;

        if (startIndex > endIndex) return -1;
        if (startIndex == endIndex)
        {
            if (comparer(array[startIndex], value) == 0) return startIndex;
            return -1;
        }

        int stringComparisonResult = comparer(array[mid], value);
        if (stringComparisonResult == 0) return mid;
        if (stringComparisonResult < 0) return FindIndexOf(value, array, comparer, mid + 1, endIndex);
        else return FindIndexOf(value, array, comparer, startIndex, mid - 1);
    }
}
