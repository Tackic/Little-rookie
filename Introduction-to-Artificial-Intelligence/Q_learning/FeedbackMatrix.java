class FeedbackMatrix {
    int get(int x, int y){
        return R[x][y];
    }
    int[] getRow(int x){
        return R[x];
    }
    private static int[][] R=
                   {{-1,0,0,0,-1,-1,-1,-1},
                    {0,-1,-1,-1,100,0,-1,-1},
                    {0,-1,-1,0,-1,-1,0,-1},
                    {0,-1,0,-1,100,-1,-1,0},
                    {-1,0,-1,0,100,-1,-1,0},
                    {-1,0,-1,-1,-1,-1,-1,0},
                    {-1,-1,0,-1,-1,-1,-1,0},
                    {-1,-1,-1,0,100,0,0,-1}};
}
