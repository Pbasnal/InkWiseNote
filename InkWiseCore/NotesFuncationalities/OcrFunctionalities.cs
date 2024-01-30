using Commons.Models;

using InkWiseNote.Commons;

using Newtonsoft.Json;

using System.Net.Http.Headers;

using Systems.SaveLoadSystem;

using UtilsLibrary;

namespace InkWiseCore.NotesFuncationalities;

public static class OcrFunctionalities
{
    public static async Task<VisionResponse> ApplyOcrOnNote(string noteName,
        Stream noteImage)
    {
        if (Objects.IsNull(noteImage))
        {
            return await Task.FromResult(new VisionResponse
            {
                readResult = new ReadResult
                {
                    content = string.Empty
                }
            });
        }

        return await Task.Factory.StartNew(() => ApplyOcr(noteName, noteImage));
    }

    private static VisionResponse ApplyOcr(string noteName, Stream imageStream)
    {
        VisionResponse visionResponse = null;
        try
        {
            string textConverstionResponse = ConvertHandwritingToText(imageStream);
            NotesFileSystem.WriteNoteToFile(Configs.PARSED_NOTES_DIRECTORY, noteName, textConverstionResponse, isJson: true);

            visionResponse = JsonConvert.DeserializeObject<VisionResponse>(textConverstionResponse);
        }
        catch (Exception ex)
        {
        }

        return visionResponse;
    }

    private static string ConvertHandwritingToText(Stream imageStream)
    {
        var client = new HttpClient();

        byte[] imageBytes = new byte[imageStream.Length];
        imageStream.Read(imageBytes, 0, imageBytes.Length);
        string contents = Convert.ToBase64String(imageBytes);
        HttpContent content = new ByteArrayContent(imageBytes);//, Encoding.UTF8, "image/*");
        content.Headers.ContentType = new MediaTypeHeaderValue("application/octet-stream");


        client.BaseAddress = new Uri(Configs.VISION_END_POINT);
        //client.DefaultRequestHeaders.Add("Content-Type", "image/png");
        client.DefaultRequestHeaders.Add("Ocp-Apim-Subscription-Key", Configs.VISION_KEY);

        var result = client.PostAsync("/computervision/imageanalysis:analyze?api-version=2023-02-01-preview&features=read&language=en", content).Result;
        string resultContent = result.Content.ReadAsStringAsync().Result;

        //VisionResponse visionResponse = JsonConvert.DeserializeObject<VisionResponse>(resultContent);

        return resultContent;
    }


    public static (bool, VisionResponse) LoadVisionResponse(string handwrittenNoteTitle)
    {
        VisionResponse visionResponse = SaveSystem.ReadFromFile<VisionResponse>(Configs.PARSED_NOTES_DIRECTORY, handwrittenNoteTitle);

        return (isVisionResponseValid(visionResponse), visionResponse);
    }

    public static bool isVisionResponseValid(VisionResponse visionResponse)
    {
        return Objects.IsNotNull(visionResponse) 
            && Objects.IsNotNull(visionResponse.readResult) 
            && !string.IsNullOrWhiteSpace(visionResponse.readResult.content);
    }
}
