import java.util.*;
public class Vehicles
{
    // here we have our gliders
    private ArrayList<Glider> listOfGliders = new ArrayList<>();

    // not sure how many planes we have, notes mention exception with certain plane count?
    // need to determine planes and how to implement

    private Plane mainPlane;

    // list of methods

    // not sure whether the following methods are acceptable

    Glider getGlider(String gliderName)
    {
        for(int i =0; i < listOfGliders.size(); i++)
        {
            if(listOfGliders.get(i).readGliderName().equals(gliderName))
            {
                return listOfGliders.get(i);
            }
        }

        // will implement exception class, for no match
        return null;
    }

    // how are glider values updated, individually in a cycle, or are we passed a glider object, and we need to make
    // copy constructor
    void createNewGlider(String gliderName)
    {
        Glider tempGlider = new Glider();

        tempGlider.setGliderName(gliderName);

        listOfGliders.add(tempGlider);
    }

    //implement update glider function, based on previous question
    // again need to implement get plane function
    // update pane function
    // do we need to implement vehicle getters for each data value of the planes and gliders, or just return the
    // object







}
