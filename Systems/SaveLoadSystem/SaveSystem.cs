using Newtonsoft.Json;

namespace Systems.SaveLoadSystem;

public class SaveSystem
{
    public static void CreateDirectoryIfNotExists(string path)
    {
        if (!Directory.Exists(path))
        {
            Directory.CreateDirectory(path);
        }
    }

    public static void SaveData(string directoryPath, string fileName, object data, bool isJson)
    {
        CreateDirectoryIfNotExists(directoryPath);
        string filePath = Path.Combine(directoryPath, $"{fileName}.json");

        string fileContentInJson = string.Empty;
        if (!isJson)
        {
            fileContentInJson = JsonConvert.SerializeObject(data, Formatting.Indented, new JsonSerializerSettings
            {
                TypeNameHandling = TypeNameHandling.Objects,
                TypeNameAssemblyFormat = System.Runtime.Serialization.Formatters.FormatterAssemblyStyle.Simple
            });
        }
        else
        {
            fileContentInJson = (string)data;
        }

        File.WriteAllText(filePath, fileContentInJson);
    }

    public static T ReadFromFile<T>(string directoryPath, string fileName)
    {
        string filePath = Path.Combine(directoryPath, $"{fileName}.json");

        if(!File.Exists(filePath)) return default(T);

        var serialisedCanvasData = File.ReadAllText(filePath);
        return JsonConvert.DeserializeObject<T>(serialisedCanvasData, new JsonSerializerSettings
        {
            TypeNameHandling = TypeNameHandling.None
        });
    }
}
